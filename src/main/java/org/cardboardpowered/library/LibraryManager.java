/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.library;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import javax.net.ssl.HttpsURLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Simple library manager which downloads external dependencies.
 */
public final class LibraryManager {

    public static final Logger logger = LogManager.getLogger("Cardboard|Libraries");

    private final File directory;
    private final boolean validateChecksums;
    private final int maxDownloadAttempts;

    private final Collection<Library> libraries;

    // Snapshot Override Paper Jar
    public static Optional<String> PAPER_OVERRIDE = Optional.empty();

    // Download timeouts (ms). A stalled connection must not hang startup forever.
    private static final int CONNECT_TIMEOUT_MS = 15_000;
    private static final int READ_TIMEOUT_MS = 60_000;

    // Backup Repos
    private static final String[] BACKUP = {
    		"https://repo.papermc.io/repository/maven-snapshots/",
    		"https://maven-central.storage-download.googleapis.com/maven2/",
    		"https://repo1.maven.org/maven2/"
    };

    /**
     * @see io/papermc/paper/plugin/loader/library/impl/MavenLibraryResolver.java
     */
    private static String getCentral() {
        String central = System.getenv("PAPER_DEFAULT_CENTRAL_REPOSITORY");
        if (central == null) { central = System.getProperty("org.bukkit.plugin.java.LibraryLoader.centralURL"); }
        if (central == null) { central = "https://maven-central.storage-download.googleapis.com/maven2"; }
        return central;
    }
    
    public static LibraryManager INSTANCE;

    /**
     * Creates the instance.
     */
    public LibraryManager(String directoryName, boolean validateChecksum, int maxDownloadAttempts, Collection<Library> libraries) {
        checkNotNull(directoryName);
        INSTANCE = this;
        this.directory = new File(directoryName);
        this.validateChecksums = validateChecksum;
        this.maxDownloadAttempts = maxDownloadAttempts;
        this.libraries = libraries;
    }
    
    public String getPaperVersion() {
    	return getLibVersion("paper-api").orElse("unknown?").replaceAll("(paper-api-[0-9.]+)-R.*(-\\d+)", "$1$2");
    }

    public Stream<Library> getLibs(String id) {
    	return libraries.stream().filter(lib -> lib.artifactId.contains(id));
    }
    
    public Library getLib(String id) {
    	for (Library lib : libraries)
    		if (lib.artifactId.contains(id))
    			return lib;
    	return null;
    }
   
    public File getJarFile(String id) {
    	Library lib = getLib(id);
    	if (null == lib) return null;
    	String fn = lib.getJarName();
    	File f = new File(directory, fn);
    	return f;
    }
    

    public String getVersion(String id) {
    	return getLibVersion(id).orElse(null);
    }
    
    public Optional<String> getLibVersion(String id) {
    	for (Library lib : libraries)
    		if (lib.artifactId.contains(id))
    			return Optional.of(lib.version);
    	return Optional.empty();
    }

    /**
     * Downloads the libraries.
     */
    public void run() {
        if (!directory.isDirectory() && !directory.mkdirs()) {
            logger.error("Could not create libraries directory: " + directory);
        }

        // Load all libraries
        for (Library lib : libraries) {
        	String fn = lib.getJarName();
        	File f = new File(directory, fn);

        	// A jar left behind by an interrupted download can be truncated and
        	// would otherwise be loaded silently (see BUG-015). Verify anything
        	// that is already on disk before trusting it.
        	if (f.isFile() && this.validateChecksums && lib.needsChecksumValidation()) {
        		if (!lib.getChecksum(f)) {
        			// fileHash stays null only when the digest could not be computed
        			// at all (unreadable file); otherwise it holds the real hash.
        			String why = (lib.fileHash == null)
        					? "could not be read/hashed"
        					: "checksum mismatch (have " + lib.fileHash + ", want " + lib.checksumValue + ")";
        			logger.warn("Existing '" + fn + "' failed verification: " + why
        					+ ". Deleting and re-downloading.");
        			if (!f.delete()) {
        				logger.warn("Could not delete '" + fn + "'.");
        			}
        		}
        	}

        	if (f.isFile()) {
        		Libraries.propose(f);
        	} else {
        		download(lib);
        	}
        }

        String det = "Paper-API: " + getPaperVersion() + "; Bungeechat: " + getVersion("bungeecord-chat") +
        		"; Adventure: " + getVersion("adventure-api") + " (" + getLibs("adventure").count() + ")";
        
        logger.info("Loaded " + libraries.size() + " libraries. " + det);
    }

    public static void main(String[] args) throws Exception {
    	for (Library l : Libraries.getLibraries()) {
			try {
				String s = l.readChecksumFromRepo(l.repository.orElse(getCentral()));
				logger.info("Library: " + l + ": Have: " + l.checksumValue + "; Need: " + s);
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	Libraries.loadLibs();
    }

    public void download(Library library) {
    	String repository = library.repository.orElse(getCentral());
    	if (!repository.endsWith("/")) repository += "/";

    	String fileName = library.getJarName();
    	File file = new File(directory, fileName);

    	// Called only when the file is missing; files that already exist are
    	// verified up front in run().
    	if (!file.exists()) {
    		attemptDownloadWithRetries(library, repository, file);
    	}

    	// Add to KnotClassLoader
    	try {
    		Libraries.propose(file);
    	} catch (Exception e) {
    		logger.warn("Failed to add to classpath: " + library, e);
    	}
    }

    private void attemptDownloadWithRetries(Library library, String repository, File file) {
    	for (int attempt = 1; attempt <= maxDownloadAttempts; attempt++) {
    		try {
    			logger.info("Downloading " + library + "...");
    			downloadPrimary(library, repository, file);

    			boolean getCheck = library.getChecksum(file);
    			if (this.validateChecksums && library.needsChecksumValidation() && !getCheck) {
    				System.out.println(getCheck);
    				if (!library.handleChecksumMismatch(repository, file, attempt, maxDownloadAttempts)) {
    					continue; // retry
    				}
    				getCheck = library.getChecksum(file);
    			}
    			if (getCheck) {
    				break;
    			}
    		} catch (IOException ex) {
    			logger.warn("Failed to download: " + library, ex);
    			file.delete();

    			if (attempt == maxDownloadAttempts) {
    				logger.warn("Restart the server to attempt downloading '" + file.getName() + "' again.");
    				return;
    			}

    			logger.warn("Retrying '" + file.getName() + "' (" + (attempt + 1) + "/" + maxDownloadAttempts + ")");
    		}
    	}
    }

    private boolean downloadPrimary(Library library, String repository, File file) throws IOException {
    	String url = library.getUrl(repository);

    	if (PAPER_OVERRIDE.isPresent() && url.contains("/paper-api/")) {
    		logger.info("Redirecting Paper-API download to: " + PAPER_OVERRIDE.get());
    		url = PAPER_OVERRIDE.get();
    	}

    	try {
    		downloadFromUrl(new URL(url), file);
    		logger.info("Downloaded " + library + '.');
    		return true;
    	} catch (FileNotFoundException ex) {
    		for (String backupRepo : BACKUP) {
    			if (downloadPrimary(library, backupRepo, file)) {
    				return true;
    			}
    		}
    		return false;
    	}
    }

    private void downloadFromUrl(URL url, File file) throws IOException {
    	HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
    	connection.setRequestProperty("User-Agent", "Mozilla/5.0 Chrome/90.0.4430.212");
    	// Without timeouts a stalled connection blocks server startup forever.
    	connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
    	connection.setReadTimeout(READ_TIMEOUT_MS);

    	// Stream into a temp file, then move it into place. Writing straight to
    	// `file` is what let a half-downloaded jar survive on disk and break the
    	// next boot (BUG-015).
    	File tmp = new File(file.getParentFile(), file.getName() + ".tmp");

    	try (
    			ReadableByteChannel input = Channels.newChannel(connection.getInputStream());
    			FileOutputStream output = new FileOutputStream(tmp)
    			) {
    		output.getChannel().transferFrom(input, 0, Long.MAX_VALUE);
    	} catch (IOException ex) {
    		tmp.delete();
    		throw ex;
    	}

    	try {
    		java.nio.file.Files.move(tmp.toPath(), file.toPath(),
    				java.nio.file.StandardCopyOption.REPLACE_EXISTING,
    				java.nio.file.StandardCopyOption.ATOMIC_MOVE);
    	} catch (java.nio.file.AtomicMoveNotSupportedException ex) {
    		// Some filesystems can't do an atomic move; a plain replace is enough
    		// here because the target is only read after this point.
    		java.nio.file.Files.move(tmp.toPath(), file.toPath(),
    				java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    	} finally {
    		java.nio.file.Files.deleteIfExists(tmp.toPath());
    	}
    }
    
    public static String[] readPackagesFromJar(File jarFile) throws IOException {
        Set<String> packages = new HashSet<>();

        try (JarFile jar = new JarFile(jarFile)) {
            jar.stream().filter(e -> !e.isDirectory()).filter(e -> e.getName().endsWith(".class"))
               .forEach(e -> {
                   String name = e.getName();
                   int slash = name.lastIndexOf('/');
                   if (slash > 0) { packages.add(name.substring(0, slash).replace('/', '.')); }
               });
        }
        Collections.addAll(packages, "org.bukkit.", "me.isaiah.", "org.cardboardpowered.", "com.", "net.", "org.", "me.");
        return packages.toArray(String[]::new);
    }

}
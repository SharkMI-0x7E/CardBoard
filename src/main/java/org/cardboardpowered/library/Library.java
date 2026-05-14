package org.cardboardpowered.library;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents a library that will be injected into the PluginClassLoader at runtime.
 */
@SuppressWarnings("deprecation")
public class Library implements Comparable<Library> {

	// LibraryKey, Parts of the group ID must be separated by periods.
	public final String groupId;
	public final String artifactId;
	public final String version;

	public Optional<String> repository;
	public HashFunction checksumType;

	public String checksumValue;
	public String fileHash;

	/**
	 * Creates a {@link Library} instance with the specified values
	 *
	 * @param checksumType Hashing.sha1() etc
	 * @param checksumValue The checksum to validate the downloaded library against.
	 */
	private Library(String groupId, String artifactId, String version, Optional<String> repository, HashFunction checksumType, String checksumValue) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.repository = repository;
		this.checksumType = checksumType;
		this.checksumValue = checksumValue;
		this.fileHash = null;
	}

	public static Library of(String groupId, String artifactId, String ver) {
		return new Library(groupId, artifactId, ver, Optional.empty(), Hashing.sha1(), null);
	}

	public static Library of(String groupId, String artifactId, String ver, String checksumValue) {
		return new Library(groupId, artifactId, ver, Optional.empty(), Hashing.sha1(), checksumValue);
	}

	public Library withSha1(String sha) {
		this.checksumType = Hashing.sha1();
		this.checksumValue = sha;
		return this;
	}

	public Library overrideRepo(String repo) {
		this.repository = Optional.of(repo);
		return this;
	}

	@Override
	public String toString() {
		return groupId + ":" + artifactId + ":" + version;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Library other = (Library) o;
		return Objects.equals(groupId, other.groupId) && Objects.equals(artifactId, other.artifactId)
				&& Objects.equals(version, other.version);
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public int compareTo(Library o) {
		return this.toString().equals(o.toString()) ? 0 : -1;
	}

	public String getJarName() {
		return artifactId + '-' + version + ".jar";
	}

	public String getUrl(String repo) {
		// Is Paper API
		String rep = repo.endsWith("/") ? repo : repo + "/";
		if (groupId.contains("io.papermc") && artifactId.contains("paper-api") ) {
			return repo + "io/papermc/paper/paper-api/" + version.split("-R0.1")[0] + "-R0.1-SNAPSHOT/paper-api-" + version + ".jar";
		}

		return rep + groupId.replace('.', '/') + '/' + artifactId + '/' + version + '/' + getJarName();
	}

	public boolean getChecksum(File file) {
		if (null == file) { throw new NullPointerException(); }
		if (!file.exists()) return false;

		if (null == this.checksumType || null == this.checksumValue || this.checksumValue.isEmpty()) return true;

		try {
			String digest = Files.hash(file, this.checksumType).toString();
			fileHash = digest;
			return digest.equals(this.checksumValue);
		} catch (IOException e) {
			LibraryManager.logger.error("Failed to compute digest for '" + file.getName() + "'", e);
			return false;
		}
	}

	public String readChecksumFromRepo(String repository) throws IOException {
		final String checksumUrl = this.getUrl(repository) + ".sha1";

		try {
			URL url = URI.create(checksumUrl).toURL();
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {

				return reader.lines().collect(Collectors.joining());
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			LibraryManager.logger.error("Invalid checksum URL");
			return null;
		}
	}

	public boolean needsChecksumValidation() {
		return checksumType != null && checksumValue != null;
	}
	
	public boolean handleChecksumMismatch(String repo, File file, int a, int b) throws IOException {
    	String remoteHash = this.readChecksumFromRepo(repo);
    	LibraryManager.logger.info("Remote checksum: " + remoteHash);

    	if (remoteHash != null && remoteHash.equals(this.fileHash)) {
    		LibraryManager.logger.info("Checksum matched for '" + file.getName() + "' (" + remoteHash + ")");
    		return true;
    	}

    	LibraryManager.logger.error("Checksum warn for '" + file.getName() + "'. Found: " + this.fileHash + ", Expect: " + this.checksumValue);

    	file.delete();
    	return a == b; // true = stop, false = retry
    }

}
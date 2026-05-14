package org.cardboardpowered.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cardboardpowered.library.LibraryManager;

/**
 */
public class JarReader {
    private static final Logger LOGGER = LogManager.getLogger("Cardboard");
    public static Set<String> found = new HashSet<>();
    private static List<String> EVENTS = new ArrayList<>();

    public static int readPlugins(File folder) throws IOException {
        LOGGER.info("Please wait, scanning plugins for events...");
        long start = System.currentTimeMillis();

        File[] files = folder.listFiles();
        if (files == null) {
            LOGGER.warn("Plugin folder is empty or unreadable.");
            return 0;
        }

        for (File file : files) {
            if (!file.getName().endsWith(".jar")) continue;

            try (JarFile jar = new JarFile(file)) {
                jar.stream()
                   .filter(e -> !e.isDirectory())
                   .filter(e -> e.getName().endsWith(".class"))
                   .forEach(entry -> scanClassEntry(jar, entry));
            }
        }

        long took = System.currentTimeMillis() - start;
        LOGGER.info("Found: " + found.size() + " (Took: " + took + "ms)");
        return found.size();
    }

    private static void scanClassEntry(JarFile jar, JarEntry entry) {
        try (InputStream in = jar.getInputStream(entry)) {
            byte[] bytes = in.readAllBytes();
            String contents = new String(bytes, StandardCharsets.ISO_8859_1);

            for (String event : EVENTS) {
                if (contents.contains(event)) {
                    found.add(event);
                }
            }
        } catch (IOException ex) {
            LOGGER.warn("Failed reading entry " + entry.getName() + " " + ex.getMessage());
        }
    }

    public static void readEvents() {
    	File jar = LibraryManager.INSTANCE.getJarFile("paper-api");
        if (null != jar) {
        	try {
				JarReader.readEventList(jar);
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
    }

    public static void readEventList(File f) throws IOException {
        LOGGER.info("Please wait, Scanning Paper-API for events...");
        long start = System.currentTimeMillis();

    	ZipFile zipFile = new ZipFile(f.getAbsolutePath());
    	Enumeration<? extends ZipEntry> entries = zipFile.entries();

    	while(entries.hasMoreElements()){
    		ZipEntry entry = entries.nextElement();
    		String name = entry.getName();
    		if (name.endsWith("Event.class")) {
    			String path = entry.getName();
    			String result = path.substring(path.lastIndexOf('/') + 1).replace(".class", "");
    			EVENTS.add(result);
    		}
    	}
    	zipFile.close();
        LOGGER.info("Found: " + EVENTS.size() + " (Took: " + (System.currentTimeMillis() - start) + "ms)");
    }

}
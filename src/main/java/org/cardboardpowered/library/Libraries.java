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

import java.io.File;
import java.util.List;
import org.cardboardpowered.CardboardConfig;

import net.fabricmc.loader.api.FabricLoader;

public class Libraries {

	/**
	 * List of our/Paper Libraries to download/load.
	 * Including: Paper-API, Adventure, Bungee-api, etc.
	 * 
	 * @implNote "paper-api" version number != Paper server version number.
	 * @see https://artifactory.papermc.io/ui/native/universe/io/papermc/paper/paper-api/
	 * @see https://github.com/PaperMC/Paper/blob/main/paper-api/build.gradle.kts
	 */
	public static List<Library> getLibraries() {
        // TODO: Keep Adventure version in check
        String adventureVersion = "4.25.0";

        // Paper API
        Library paperApi = Library.of("io.papermc", "paper-api", "1.21.11-R0.1-20260120.191825-59")
        		.withSha1("223f4b673a6cefe155849a18d7a82b422bf45335")
        		.overrideRepo("https://repo.papermc.io/repository/maven-snapshots/");

        List<Library> libraries = List.of(
        	paperApi,
        	// Paper API Libraries
        	Library.of("org.xerial", "sqlite-jdbc", "3.41.0.0", "86168d5ae9bfc54dab9c47cd6e1af751c1d15eb3"),
        	Library.of("com.mysql", "mysql-connector-j", "8.0.32", "41ec3f8cdaccf6c46a47d7cd628eeb59a926d9d4"),
        	Library.of("commons-lang", "commons-lang", "2.6", "0ce1edb914c94ebc388f086c6827e8bdeec71ac2"),
        	Library.of("org.apache.commons", "commons-collections4", "4.4", "62ebe7544cb7164d87e0637a2a6a2bdc981395e8"),
        	Library.of("commons-collections", "commons-collections", "3.2.1", "761ea405b9b37ced573d2df0d1e3a4e0f9edc668"),
        	Library.of("net.md-5", "bungeecord-chat", "1.21-R0.2", "64956ff493786f981a15697ce406fe39a2551692"),
        	// Adventure
        	Library.of("net.kyori", "adventure-api", adventureVersion, "6bd10494eeb2f8eadce7226db4445e8728985cbb"),
        	Library.of("net.kyori", "adventure-key", adventureVersion, "eadeff9eeaa46f76de3f31fdff1d8e952273cf04") ,
        	Library.of("net.kyori", "adventure-text-serializer-gson", adventureVersion, "e312e240fe82f4207ff2232b33ee4433855bdfff") ,
        	Library.of("net.kyori", "adventure-text-serializer-json", adventureVersion, "ff6b4381dd8be9a40a1127937a4b71b9b010fcd6") ,
        	Library.of("net.kyori", "adventure-text-serializer-commons", adventureVersion, "58708c96ea4292800f08360ca1ce8a31ef0cdf97") ,
        	Library.of("net.kyori", "adventure-text-serializer-legacy", adventureVersion, "b12eaaac78d2534b9b1556049a8d95a046b0812d") ,
        	Library.of("net.kyori", "adventure-text-serializer-plain", adventureVersion, "82f5d4188f3cb6da9654b4ceea8b4093af5f1243") ,
        	Library.of("net.kyori", "adventure-text-minimessage", adventureVersion, "38f8f778c92f1ea848f79f992c99c4b98f96f23b") ,
        	Library.of("net.kyori", "option", "1.1.0", "593fecb9c42688eebc7d8da5d6ea127f4d4c92a2"),

        	// Maven Resolver (Aether) + friends: required by paper-api's
        	// org.bukkit.plugin.java.LibraryLoader, which resolves the
        	// plugin.yml "libraries:" lists. Without these classes on the
        	// runtime classpath the LibraryLoader constructor throws
        	// NoClassDefFoundError and the whole "libraries:" feature stays
        	// silently disabled for every plugin (see BUG-023).
        	// Versions mirror the dependency tree paper-api 3.9.6 compiles
        	// against; keep them in sync when paper-api is updated.
        	// commons-logging is easy to miss: httpclient 4.5.x needs it and
        	// without it the failure only surfaces at the first real download.
        	Library.of("org.apache.maven", "maven-resolver-provider", "3.9.6", "848c45d334f6cc5c8dd602b0e58fd4482964eddc"),
        	Library.of("org.apache.maven", "maven-model", "3.9.6", "ac9a1c8a8cfa36f3a5489837e653ec0cd530d576"),
        	Library.of("org.apache.maven", "maven-artifact", "3.9.6", "fb0979832c10c1a25d038a33ca862bef055fcdc8"),
        	Library.of("org.apache.maven", "maven-model-builder", "3.9.6", "983ce00d50a9f78ad1b805e21e4fd71807fa6ebf"),
        	Library.of("org.apache.maven", "maven-builder-support", "3.9.6", "bcfc9d8175eaba21111edf21e0355a8523461abc"),
        	Library.of("org.apache.maven", "maven-repository-metadata", "3.9.6", "497cda3149f3c661113f9a663e0270ce2566cc95"),
        	Library.of("org.apache.maven.resolver", "maven-resolver-api", "1.9.18", "0cd5174d6e80175398debe4869d484169c0abbf8"),
        	Library.of("org.apache.maven.resolver", "maven-resolver-spi", "1.9.18", "7fa176b3353ef6d78d02db39e025f3c27a983158"),
        	Library.of("org.apache.maven.resolver", "maven-resolver-util", "1.9.18", "5ae9406f188ae4a999c353fce3fd77273797a216"),
        	Library.of("org.apache.maven.resolver", "maven-resolver-impl", "1.9.18", "e928b128d1e52e6299f94431ce3df74647bc8c26"),
        	Library.of("org.apache.maven.resolver", "maven-resolver-named-locks", "1.9.18", "31f948d89dcb3d9739e70d5e1000ebd68eb4405d"),
        	Library.of("org.apache.maven.resolver", "maven-resolver-connector-basic", "1.9.18", "baac1ca4eb5e5fbdd2df554262a1b97f84ae3cec"),
        	Library.of("org.apache.maven.resolver", "maven-resolver-transport-http", "1.9.18", "61a5512ff44502a5b22800f097f43281cb934a72"),
        	Library.of("org.codehaus.plexus", "plexus-utils", "3.5.1", "c6bfb17c97ecc8863e88778ea301be742c62b06d"),
        	Library.of("org.codehaus.plexus", "plexus-interpolation", "1.26", "25b919c664b79795ccde0ede5cee0fd68b544197"),
        	Library.of("javax.inject", "javax.inject", "1", "6975da39a7040257bd51d21a231b76c915872d38"),
        	Library.of("org.eclipse.sisu", "org.eclipse.sisu.inject", "0.9.0.M2", "5ace70e1ea696d156f5034a42a615df13a52003a"),
        	Library.of("com.google.guava", "guava", "33.3.1-jre", "852f8b363da0111e819460021ca693cacca3e8db"),
        	Library.of("com.google.guava", "failureaccess", "1.0.2", "c4a06a64e650562f30b7bf9aaec1bfed43aca12b"),
        	Library.of("org.apache.httpcomponents", "httpclient", "4.5.14", "1194890e6f56ec29177673f2f12d0b8e627dec98"),
        	Library.of("org.apache.httpcomponents", "httpcore", "4.4.16", "51cf043c87253c9f58b539c9f7e44c8894223850"),
        	Library.of("commons-logging", "commons-logging", "1.2", "4bfc12adfe4842bf07b657f0369c4cb522955686"),
        	Library.of("commons-codec", "commons-codec", "1.16.0", "4e3eb3d79888d76b54e28b350915b5dc3919c9de"),
        	Library.of("org.apache.commons", "commons-lang3", "3.12.0", "c6842c86792ff03b9f1d1fe2aab8dc23aa6c6f0e"),
        	Library.of("org.slf4j", "slf4j-api", "2.0.17", "d9e58ac9c7779ba3bf8142aff6c830617a7fe60f")
        );

        // Set WorldEdit adapter class name here
        // as this provides more verbose stacktraces.
        System.setProperty("worldedit.bukkit.adapter", "com.sk89q.worldedit.bukkit.adapter.impl.v1_21_11.PaperweightAdapter");

        return libraries;
    }

	/**
	 * Runs a new LibraryManager with the {@link #getLibraries()} list,
	 * after making sure a reachable maven mirror is configured.
	 */
	public static void loadLibs() {
    	List<Library> libraries = getLibraries();

    	LibraryManager.ensureUsableCentral();

    	LibraryManager man = new LibraryManager("lib", true, 2, libraries);
    	man.run();
    }

    /**
     * Add a jar file to Fabric's Knot Classloader.
     * 
     * @implSpec If Dev Env, will skip adding, assuming already in dev classpath.
     * @implNote If debug print is True, file name will be logged.
     * @return True, or False if Exception thrown.
     */
    public static boolean propose(File file) {
        try {
        	if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            	net.fabricmc.loader.impl.launch.FabricLauncherBase.getLauncher().addToClassPath(file.toPath(), LibraryManager.readPackagesFromJar(file));
            }

            if (CardboardConfig.DEBUG_VERBOSE_CALLS) {
            	LibraryManager.logger.info("Debug: Loading library " + file.getName());
            }
            return true;
        } catch (Exception e) {
            // Name the offending library explicitly. The previous message
            // ("... while accessing Fabric Loader") was hard-coded and actively
            // misleading: it made a corrupt dependency jar look like a Fabric
            // Loader problem. See BUG-015.
            LibraryManager.logger.error("Failed to load library '" + file.getName() + "': "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
            LibraryManager.logger.error("Hint: if the jar is corrupt, delete it and restart to re-download.");
            return false;
        }
    }

}
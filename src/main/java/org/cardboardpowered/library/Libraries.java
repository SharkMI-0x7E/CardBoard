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
        	Library.of("net.kyori", "option", "1.1.0", "593fecb9c42688eebc7d8da5d6ea127f4d4c92a2")
        );

        // Set WorldEdit adapter class name here
        // as this provides more verbose stacktraces.
        System.setProperty("worldedit.bukkit.adapter", "com.sk89q.worldedit.bukkit.adapter.impl.v1_21_11.PaperweightAdapter");

        return libraries;
    }

	/**
	 * Runs a new LibraryManager with the {@link #getLibraries()} list,
	 */
	public static void loadLibs() {
    	List<Library> libraries = getLibraries();

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
            LibraryManager.logger.error("ERR: \"" + e.getMessage() + "\" while accessing Fabric Loader.");
            return false;
        }
    }

}
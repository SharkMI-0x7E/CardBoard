package org.cardboardpowered;

import me.isaiah.config.FileConfiguration;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.cardboardpowered.library.Libraries;

public class CardboardConfig {

	private static final int CONFIG_VERSION = 1;
    
	private static DefaultConfig DEFAULT_CONF = new DefaultConfig()
			.addSection(new ConfigSection()
				.comments(
						"# This is the Configuration file for Cardboard",
			 		   "# config-version internal use, do not modify"
				)
				.keys("config-version")
				.values("" + CONFIG_VERSION)
			)
			.addSection(new ConfigSection("chat-mixin")
				.comments(
						"# Invoke ChatEvent from PlayerManager instead of NetworkHandler",
						"# This can solve issues with other mods that overwrite the chat method,"
				)
				.keys("use_alternative_chat_mixin")
				.values("false")
			)
			.addSection(new ConfigSection("registry-command-fix")
				.comments(
						"# Registry Command Fix",
						"# Commands like \"/give\" or \"/setblock\" don't work when",
						"# executed by entities unless \"minecraft:\" prefix is specified.",
						"# Enabling will break chat signatures."
				)
				.keys("registry-command-fix")
				.values("true")
			)
			.addSection(new ConfigSection("reflection-remapper-skip")
				.comments(
						"# Reflection Remapper Skip",
						"# Our current Reflection remapper might cause issues with some plugins",
						"# You can add plugin names here (that dont use Reflection) to our SKIP array"
				)
				.keys("skip_reflection_for_plugin")
				.values("\n\t- vault\n\t- worldguard")
			)
			.addSection(new ConfigSection("forcefully-disable-mixins")
				.comments(
						"# Forcefully Disable Mixins - If a mixin is causing an issue you can disable it here"
				)
				.keys("mixin-force-disable")
				.values("\n\t- None")
			)
			.addSection(new ConfigSection("register-mods-command")
				.comments(
						"# Register '/mods' Command",
						"# This will add a command to view all Fabric mods similar to /plugins"
				)
				.keys("add-mods-command")
				.values("true")
			)
			.addSection(new ConfigSection("prefix-loggers")
				.comments(
						"# Console Logging - ",
						"# \tLog Prefix: Add a prefix to loggers with the plugins's ID like in Paper (ex: '[Essentials]')",
						"# \tColor Strip: will strip all Bukkit ChatColor info from Console text",
						"# (If BetterFabricConsole is present, these will override to false & false)"
				)
				.keys("prefix-loggers", "should-strip-console-color")
				.values("true", "false")
			)
			.addSection(new ConfigSection("debug-stuff")
				.comments(
						"# Debug Test Stuff"
				)
				.keys("debug_print_event_call", "debug_print_all_calls", "debug_player", "debug_other", "debug_print_remaputil")
				.values("false", "false", "false", "false", "false")
			);


	private static class DefaultConfig {
		private ArrayList<ConfigSection> sections;
		
		public DefaultConfig() {
			this.sections = new ArrayList<>();
		}
		
		public DefaultConfig addSection(ConfigSection sec) {
			this.sections.add(sec);
			return this;
		}
		
		public String asString() {
			return sections.stream()
					.map(ConfigSection::asString)
					.collect(Collectors.joining("\n"));
		}
	}
	
	private static class ConfigSection {
		
		private String name;
		private String[] comments;
		private String[] keys;
		private String[] values;
		
		public ConfigSection() {
			this.name = "";
		}
		
		public ConfigSection(String name) {
			this.name = name;
		}
		
		public ConfigSection comments(String... strs) {
			this.comments = strs;
			return this;
		}
		
		public ConfigSection keys(String... strs) {
			this.keys = strs;
			return this;
		}
		
		public ConfigSection values(String... strs) {
			this.values = strs;
			return this;
		}

		public String joinKeyValuePairs() {
			return IntStream.range(0, Math.min(keys.length, values.length))
							.mapToObj(i -> keys[i] + (values[i].contains("\n") ? ":" : ": ") + values[i])
							.collect(Collectors.joining("\n"));
		}

		public String asString() {
			return String.join("\n", comments) + "\n" + joinKeyValuePairs() + "\n";
		}

	}

	private static final String DEF_CONFIG_TEXT = DEFAULT_CONF.asString();

    public static ArrayList<String> disabledMixins = new ArrayList<>();
    public static boolean ALT_CHAT = false;
	public static boolean REGISTRY_COMMAND_FIX = true;
	
    public static boolean DEBUG_EVENT_CALL = false;
    public static boolean DEBUG_VERBOSE_CALLS = false;
    public static boolean DEBUG_OTHER = false;
    public static boolean DEBUG_PLAYER = false;
    public static boolean DEBUG_LOG_REMAP = false;
    public static boolean DEBUG_REMAP_WE = false;

	public static boolean addModsCommand = true;
	public static boolean addPluginPrefixToLogger = true;
	public static boolean shouldStripConsoleColor = false;

    public static void setup() throws Exception {
        File fabDir = FabricLoader.getInstance().getConfigDir().toFile();
        File oldDir = new File(fabDir, "bukkit4fabric");
        File dir = new File(fabDir, "cardboard");
        if (oldDir.exists()) {
            for (File fi : oldDir.listFiles()) fi.renameTo(new File(dir, fi.getName()));
            oldDir.delete();
        }

        dir.mkdirs();
        File f = new File(dir, "cardboard-config.yml");
        save_default(f);

        File oldConfig = new File(fabDir, "cardboard.yml");
        if (oldConfig.exists()) {
            migrate_config(oldConfig, f);
        }

        FileConfiguration config = new FileConfiguration(f);
        ALT_CHAT = config.getBoolean("use_alternative_chat_mixin");
		REGISTRY_COMMAND_FIX = config.getBoolean("registry-command-fix");

        ArrayList<String> disables = (ArrayList<String>)config.getObject("mixin-force-disable");
        disabledMixins.addAll(disables);
        
        try {
        	setup_debug(config);
        } catch (Exception ignore) {
        	// Entry does not exist.
        }
        
        addModsCommand = config.getOrDefault("add-mods-command", true);
        addPluginPrefixToLogger = config.getOrDefault("prefix-plugin-logger", true);
        shouldStripConsoleColor = config.getOrDefault("should-strip-console-color", false);
        
        if (shouldStripConsoleColor && isBetterConsole()) {
        	shouldStripConsoleColor = false;
        }
    }
    
    public static boolean shouldAddPrefixToLoggers() {
    	return addPluginPrefixToLogger && !isBetterConsole();
    }
    
    public static boolean isBetterConsole() {
    	return checkIfModLoaded("better-fabric-console") || checkIfModLoaded("org_jline_jline");
    }
    
    private static boolean checkIfModLoaded(String modId) {
    	boolean isLoaded = FabricLoader.getInstance().isModLoaded(modId);
    	boolean isPresent = FabricLoader.getInstance().getModContainer(modId).isPresent();
    	return isLoaded || isPresent;
    }
    
    private static void setup_debug(FileConfiguration config) throws Exception {
        DEBUG_EVENT_CALL = config.getOrDefault("debug_print_event_call", false);
        DEBUG_VERBOSE_CALLS =  config.getOrDefault("debug_print_all_calls", false);
        DEBUG_OTHER = config.getOrDefault("debug_other", false);
        DEBUG_PLAYER = config.getOrDefault("debug_player", false);
        DEBUG_LOG_REMAP = config.getOrDefault("debug_print_remaputil", false);
        DEBUG_REMAP_WE = config.getOrDefault("debug_print_remap_for_worldedit", false);

        String extraJar = config.getOrDefault("debug_extra_lib_file", "debug_extra.jar");
        if (extraJar.length() > 2) {
        	File file = new File(new File("lib"), extraJar);
            // Add to KnotClassLoader
        	if (file.exists()) {
	            try {
	                Libraries.propose(file);
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
        	}
        	
        }
    }

    private static void save_default(File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
            Files.write(file.toPath(), DEF_CONFIG_TEXT.getBytes());
        }
    }

    private static void migrate_config(File oldConfig, File newConfig) throws IOException {
        System.out.println("Migrating old configuration...");
        for (String line : Files.readAllLines(oldConfig.toPath())) {
            if (line.startsWith("#")) continue;
            if (line.indexOf('=') != -1) {
                line = line.trim();
                String val = line.split("=")[1];
                if (line.startsWith("use_alternative_chat_mixin")) ALT_CHAT = Boolean.valueOf(val);
                if (line.startsWith("mixin_force_disable")) {
                    if (val.startsWith("org.cardboardpowered.mixin."))
                        disabledMixins.add(val);
                    else disabledMixins.add("org.cardboardpowered.mixin." + val);
                }
            }
        }

        StringBuilder con = new StringBuilder();
        for (String line : Files.readAllLines(newConfig.toPath())){con.append(line).append("\n");}
        con = new StringBuilder(con.toString()
		        .replace("use_alternative_chat_mixin: false", "use_alternative_chat_mixin: " + ALT_CHAT));
        Files.write(newConfig.toPath(), con.toString().getBytes());
        oldConfig.delete();
    }

}

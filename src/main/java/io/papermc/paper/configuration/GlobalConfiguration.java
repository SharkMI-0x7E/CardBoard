package io.papermc.paper.configuration;

import org.bukkit.Bukkit;

public class GlobalConfiguration {

    static final int CURRENT_VERSION = 30;
    private static GlobalConfiguration instance;
    public static boolean isFirstStart;

    public int version = 30;

    public static GlobalConfiguration get() {
        GlobalConfiguration current = instance;
        if (current == null) {
            // Paper loads this from paper-global.yml during startup; Cardboard has no such
            // loader, so fall back to a defaults instance instead of returning null - callers
            // like CraftScoreboardManager dereference get() unconditionally.
            current = new GlobalConfiguration();
            instance = current;
        }
        return current;
    }

    static void set(GlobalConfiguration instance) {
        GlobalConfiguration.instance = instance;
    }

    static {
        isFirstStart = false;
    }
    
    public class Proxies {
        public boolean isProxyOnlineMode() {
            return Bukkit.getOnlineMode(); /*|| SpigotConfig.bungee && this.bungeeCord.onlineMode || this.velocity.enabled && this.velocity.onlineMode;*/
        }
    }
    public Scoreboards scoreboards = new Scoreboards();

    public class Scoreboards { //extends ConfigurationPart {
        public boolean trackPluginScoreboards = false;
        public boolean saveEmptyScoreboardTeams = true;
    }
}
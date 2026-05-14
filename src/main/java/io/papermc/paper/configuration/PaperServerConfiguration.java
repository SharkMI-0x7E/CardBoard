package io.papermc.paper.configuration;

import org.bukkit.Bukkit;
import org.spigotmc.SpigotConfig;

public class PaperServerConfiguration implements ServerConfiguration {

    public boolean isProxyOnlineMode() {
    	return Bukkit.getOnlineMode() || SpigotConfig.bungee;
    	//  return GlobalConfiguration.get().proxies.isProxyOnlineMode();
    }

    public boolean isProxyEnabled() {
        return SpigotConfig.bungee;
    	//return GlobalConfiguration.get().proxies.velocity.enabled || SpigotConfig.bungee;
    }

}
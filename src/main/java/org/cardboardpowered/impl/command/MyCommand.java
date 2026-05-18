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
package org.cardboardpowered.impl.command;

import com.google.common.collect.ImmutableList;

import net.fabricmc.loader.api.FabricLoader;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.cardboardpowered.CardboardConfig;
import org.cardboardpowered.CardboardMod;

import java.util.Arrays;
import java.util.List;

/**
 * Provides a /cardboard command
 */
public class MyCommand extends Command {

    public MyCommand() {
        super("cardboardtest");

        this.description = "Testing";
        this.usageMessage = "/cardboardtest";
        
        List<String> aka = Arrays.asList("cardboarddebug", "cardboardebug", "cardboard");
        
        this.setAliases(aka);
        this.setPermission("cardboard.command.admin");
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
    	if (!sender.hasPermission("cardboard.command.admin")) {
    		return false;
    	}
    	
    	if (args.length == 0) {
    		sender.sendMessage("Usage: /cardboardtest <arg>: arg = debugverbose; worlds");
    		return true;
    	}
    	
    	if (args[0].contains("debugverbose")) {
    		CardboardConfig.DEBUG_VERBOSE_CALLS = !CardboardConfig.DEBUG_VERBOSE_CALLS;
            sender.sendMessage("DEBUG_VERBOSE_CALLS: " +CardboardConfig.DEBUG_VERBOSE_CALLS);
    	}
    	
    	if (args[0].contains("debugevents")) {
    		CardboardConfig.DEBUG_EVENT_CALL = !CardboardConfig.DEBUG_EVENT_CALL;
            sender.sendMessage("Logging event calls to console: " +CardboardConfig.DEBUG_EVENT_CALL);
    	}
    	
    	if (args[0].equalsIgnoreCase("worlds")) {
    		List<World> worlds = Bukkit.getWorlds();
    		sender.sendMessage("Testing output of \"Bukkit.getWorlds()\":");
    		for (World w : worlds) {
    			sender.sendMessage("- WORLD: " + w.getName() + " with player count: " + w.getPlayerCount());
    		}
    	}
    	
    	if (args[0].equalsIgnoreCase("version")) {
    		String ver = FabricLoader.getInstance().getModContainer("cardboard").get().getMetadata().getVersion().getFriendlyString();
            if (ver.contains("version")) ver = CraftServer.INSTANCE.getShortVersion(); // Dev ENV

            String message = ChatColor.GOLD + "Cardboard" + ChatColor.RESET + " version " + ver + ChatColor.ITALIC + " (Reimplementing Paper API version " + CardboardMod.paperVersion + ")";
            sender.sendMessage(message);
    	}
    	
    	// Reload Config
    	if (args[0].equalsIgnoreCase("reload")) {
    		sender.sendMessage("Reloading Cardboard config.yml.");
    		try {
				CardboardConfig.setup();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
    	
    	if (args.length == 0) {
    		return ImmutableList.of("debugverbose", "worlds", "version", "reload");
    	}
    	
        return ImmutableList.of();
    }

}

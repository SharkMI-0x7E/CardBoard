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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.StringUtil;
import org.cardboardpowered.compat.ModCompatibilityDatabase;
import org.cardboardpowered.compat.ModCompatibilityRule;
import org.cardboardpowered.util.Messages;

import com.google.common.collect.ImmutableList;

/**
 * Cardboard's replacement for the vanilla {@code PluginsCommand}.
 *
 * <p>The vanilla command dumps one coloured line and nothing else. This version
 * makes the status readable (green = enabled, red = disabled, plus an explicit
 * enabled/total count), adds {@code /plugins info <name>}, and can actually
 * enable/disable a plugin at runtime via {@code /plugins enable|disable <name>}.
 *
 * <p>Disabling also unregisters the plugin's commands (see
 * {@code BukkitSimplePluginManagerMixin.disablePlugin}) so no "zombie" commands
 * are left behind; the client command tree is re-synced afterwards.
 */
public class CardboardPluginsCommand extends Command {

    public CardboardPluginsCommand(String name) {
        super(name);

        this.description = "Gets a list of plugins running on the server";
        this.usageMessage = "/plugins [info|enable|disable <name>]";
        this.setAliases(Arrays.asList("pl"));
        this.setPermission("bukkit.command.plugins");
    }

    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!testPermission(sender)) {
            return true;
        }
        if (args.length == 0) {
            return list(sender);
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("info")) {
            if (args.length < 2) {
                sender.sendMessage(Messages.get(sender, "plugins.usage"));
                return true;
            }
            return info(sender, join(args, 1));
        }
        if (sub.equals("enable") || sub.equals("disable")) {
            if (!sender.hasPermission("cardboard.command.admin")) {
                sender.sendMessage(Messages.get(sender, "cmd.no-permission"));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(Messages.get(sender, "plugins.usage"));
                return true;
            }
            return setEnabled(sender, join(args, 1), sub.equals("enable"));
        }
        sender.sendMessage(Messages.get(sender, "plugins.usage"));
        return true;
    }

    private boolean list(CommandSender sender) {
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        int enabled = 0;
        StringBuilder names = new StringBuilder();
        for (Plugin plugin : plugins) {
            if (plugin.isEnabled()) {
                enabled++;
            }
            if (names.length() > 0) {
                names.append(ChatColor.GRAY).append(", ");
            }
            names.append(plugin.isEnabled() ? ChatColor.GREEN : ChatColor.RED).append(plugin.getName());
        }

        sender.sendMessage(Messages.get(sender, "plugins.title", enabled, plugins.length));
        if (plugins.length == 0) {
            sender.sendMessage(Messages.get(sender, "plugins.empty"));
        } else {
            sender.sendMessage("  " + names);
            sender.sendMessage(Messages.get(sender, "plugins.legend"));
        }
        return true;
    }

    private boolean info(CommandSender sender, String name) {
        Plugin plugin = findPlugin(name);
        if (plugin == null) {
            sender.sendMessage(Messages.get(sender, "plugins.info.not-found", name));
            return true;
        }

        PluginDescriptionFile desc = plugin.getDescription();
        sender.sendMessage(Messages.get(sender, "plugins.info.header", desc.getName()));
        sender.sendMessage(Messages.get(sender, "plugins.info.version", desc.getVersion()));
        if (!desc.getAuthors().isEmpty()) {
            sender.sendMessage(Messages.get(sender, "plugins.info.authors", String.join(", ", desc.getAuthors())));
        }
        if (desc.getWebsite() != null) {
            sender.sendMessage(Messages.get(sender, "plugins.info.website", desc.getWebsite()));
        }
        sender.sendMessage(Messages.get(sender, plugin.isEnabled() ? "plugins.info.enabled" : "plugins.info.disabled"));

        ModCompatibilityRule rule = findCompatRule(plugin.getName());
        if (rule != null) {
            sender.sendMessage(Messages.get(sender, "plugins.info.compat",
                    Messages.get(sender, statusKey(rule.getStatus())), rule.getNotes()));
        }
        return true;
    }

    private boolean setEnabled(CommandSender sender, String name, boolean enable) {
        Plugin plugin = findPlugin(name);
        if (plugin == null) {
            sender.sendMessage(Messages.get(sender, "plugins.info.not-found", name));
            return true;
        }
        if (plugin.isEnabled() == enable) {
            sender.sendMessage(Messages.get(sender,
                    enable ? "plugins.already-enabled" : "plugins.already-disabled", plugin.getName()));
            return true;
        }

        try {
            if (enable) {
                Bukkit.getPluginManager().enablePlugin(plugin);
                resyncCommands();
                sender.sendMessage(Messages.get(sender, "plugins.enable.done", plugin.getName()));
            } else {
                Bukkit.getPluginManager().disablePlugin(plugin);
                resyncCommands();
                sender.sendMessage(Messages.get(sender, "plugins.disable.done", plugin.getName()));
            }
        } catch (Throwable t) {
            sender.sendMessage(Messages.get(sender,
                    enable ? "plugins.enable.failed" : "plugins.disable.failed",
                    plugin.getName(), String.valueOf(t.getMessage())));
        }
        return true;
    }

    private static void resyncCommands() {
        if (Bukkit.getServer() instanceof CraftServer craftServer) {
            craftServer.resyncCommands();
        }
    }

    private static ModCompatibilityRule findCompatRule(String pluginName) {
        ModCompatibilityDatabase db = ModCompatibilityDatabase.load();
        ModCompatibilityRule rule = db.getRuleForMod(pluginName).orElse(null);
        if (rule == null) {
            rule = db.getRuleForMod(pluginName.toLowerCase(Locale.ROOT)).orElse(null);
        }
        return rule;
    }

    private static String statusKey(ModCompatibilityRule.Status status) {
        switch (status) {
            case COMPATIBLE:
                return "compat.status.compatible";
            case CONFLICT_RESOLVED:
                return "compat.status.resolved";
            default:
                return "compat.status.investigation";
        }
    }

    private static Plugin findPlugin(String name) {
        Plugin exact = Bukkit.getPluginManager().getPlugin(name);
        if (exact != null) {
            return exact;
        }
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin.getName().equalsIgnoreCase(name)) {
                return plugin;
            }
        }
        return null;
    }

    private static String join(String[] args, int from) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < args.length; i++) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(args[i]);
        }
        return sb.toString();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            return prefixMatches(Arrays.asList("info", "enable", "disable"), args[0]);
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("info") || sub.equals("enable") || sub.equals("disable")) {
                List<String> names = new ArrayList<>();
                for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                    names.add(plugin.getName());
                }
                return prefixMatches(names, args[1]);
            }
        }
        return ImmutableList.of();
    }

    private static List<String> prefixMatches(List<String> options, String prefix) {
        List<String> out = new ArrayList<>();
        for (String option : options) {
            if (StringUtil.startsWithIgnoreCase(option, prefix)) {
                out.add(option);
            }
        }
        return out;
    }

}

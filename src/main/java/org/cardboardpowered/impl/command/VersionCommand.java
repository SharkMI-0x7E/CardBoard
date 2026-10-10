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
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.StringUtil;
import org.cardboardpowered.util.Messages;

import net.fabricmc.loader.api.FabricLoader;

public class VersionCommand extends Command {

    public static String BRANCH = "ver/1.21.4";

    public VersionCommand(String name) {
        super(name);

        this.description = "Gets the version of this server including any plugins in use";
        this.usageMessage = "/version [plugin name]";
        this.setPermission("bukkit.command.version");
        this.setAliases(Arrays.asList("ver", "about", "version"));
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!testPermission(sender)) return true;

        if (args.length == 0) {
            String ver = FabricLoader.getInstance().getModContainer("cardboard").get().getMetadata().getVersion().getFriendlyString();
            if (ver.contains("version")) ver = CraftServer.INSTANCE.getShortVersion(); // Dev ENV

            sender.sendMessage(Messages.get(sender, "version.line", Bukkit.getName(), ver, Bukkit.getBukkitVersion()));
            sendVersion(sender);
        } else {
            StringBuilder name = new StringBuilder();

            for (String arg : args) {
                if (name.length() > 0) name.append(' ');
                name.append(arg);
            }

            String pluginName = name.toString();
            Plugin exactPlugin = Bukkit.getPluginManager().getPlugin(pluginName);
            if (exactPlugin != null) {
                describeToSender(exactPlugin, sender);
                return true;
            }

            boolean found = false;
            pluginName = pluginName.toLowerCase(java.util.Locale.ENGLISH);
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                if (plugin.getName().toLowerCase(java.util.Locale.ENGLISH).contains(pluginName)) {
                    describeToSender(plugin, sender);
                    found = true;
                }
            }

            if (!found) {
                sender.sendMessage(Messages.get(sender, "version.plugin-not-found"));
                sender.sendMessage(Messages.get(sender, "version.use-plugins"));
            }
        }
        return true;
    }

    private void describeToSender(Plugin plugin, CommandSender sender) {
        PluginDescriptionFile desc = plugin.getDescription();
        sender.sendMessage(Messages.get(sender, "plugins.info.header", desc.getName()));
        sender.sendMessage(Messages.get(sender, "plugins.info.version", desc.getVersion()));

        if (desc.getDescription() != null) sender.sendMessage(desc.getDescription());
        if (desc.getWebsite() != null)     sender.sendMessage(Messages.get(sender, "plugins.info.website", desc.getWebsite()));
        if (!desc.getAuthors().isEmpty())  sender.sendMessage(Messages.get(sender, "plugins.info.authors", getAuthors(desc)));
    }

    private String getAuthors(final PluginDescriptionFile desc) {
        StringBuilder result = new StringBuilder();
        List<String> authors = desc.getAuthors();

        for (int i = 0; i < authors.size(); i++) {
            if (result.length() > 0) {
                result.append(org.bukkit.ChatColor.WHITE);
                result.append(i < authors.size() - 1 ? ", " : " and ");
            }

            result.append(org.bukkit.ChatColor.GREEN);
            result.append(authors.get(i));
        }

        return result.toString();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");
        Validate.notNull(alias, "Alias cannot be null");

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String toComplete = args[0].toLowerCase(java.util.Locale.ENGLISH);
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
                if (StringUtil.startsWithIgnoreCase(plugin.getName(), toComplete))
                    completions.add(plugin.getName());

            return completions;
        }
        return ImmutableList.of();
    }

    private final ReentrantLock versionLock = new ReentrantLock();
    private boolean hasVersion = false;
    private String versionKey = null;
    private Object[] versionArgs = new Object[0];
    private final Set<CommandSender> versionWaiters = new HashSet<>();
    private boolean versionTaskStarted = false;
    private long lastCheck = 0;

    private void sendVersion(CommandSender sender) {
        if (hasVersion) {
            if (System.currentTimeMillis() - lastCheck > 21600000) {
                lastCheck = System.currentTimeMillis();
                hasVersion = false;
            } else {
                sender.sendMessage(Messages.get(sender, versionKey, versionArgs));
                return;
            }
        }
        versionLock.lock();
        try {
            if (hasVersion) {
                sender.sendMessage(Messages.get(sender, versionKey, versionArgs));
                return;
            }
            versionWaiters.add(sender);
            sender.sendMessage(Messages.get(sender, "version.checking"));
            if (!versionTaskStarted) {
                versionTaskStarted = true;
                Thread versionThread = new Thread(this::obtainVersion, "Cardboard Version Check");
                versionThread.setDaemon(true); // BUG-019: must not block JVM exit
                versionThread.start();
            }
        } finally {
            versionLock.unlock();
        }
    }

    private void obtainVersion() {
        try {
            String version = Bukkit.getVersion();
            if (version == null) version = "Custom";

            if (version.startsWith("git-Cardboard-")) {
                int cbVersions = check();
                if (cbVersions == 0) {
                    setVersion("version.latest");
                } else if (cbVersions > 0) {
                    setVersion("version.behind", cbVersions);
                } else {
                    // negative sentinel = check failed / unknown commit / custom build
                    setVersion("version.custom");
                }
            } else {
                setVersion("version.custom");
            }
        } catch (Throwable t) {
            // never leave waiters stuck on "checking..."
            setVersion("version.custom");
        }
    }

    private void setVersion(String key, Object... args) {
        lastCheck = System.currentTimeMillis();
        versionKey = key;
        versionArgs = args;
        versionLock.lock();
        try {
            hasVersion = true;
            versionTaskStarted = false;
            for (CommandSender sender : versionWaiters)
                sender.sendMessage(Messages.get(sender, versionKey, versionArgs));
            versionWaiters.clear();
        } finally {
            versionLock.unlock();
        }
    }

    public static String getGitHash() {
        try {
            Class<?> version = Class.forName("org.cardboardpowered.GitVersion");
            return (String) version.getField("GIT_SHA").get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            return "-unknown-";
        }
    }

    public static boolean isDirty() {
        try {
            Class<?> version = Class.forName("org.cardboardpowered.GitVersion");
            return ( (Integer) version.getField("DIRTY").get(null) ) == 1;
        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            return false;
        }
    }

    public static int check() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://api.github.com/repos/CardboardPowered/cardboard/compare/" + BRANCH + "..." + getGitHash()).openConnection();
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) return -2; // Unknown commit

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            JsonObject obj = new Gson().fromJson(reader, JsonObject.class);
            String status = obj.get("status").getAsString();

            if (status.equalsIgnoreCase("identical")) return 0;
            if (status.equalsIgnoreCase("behind")) return obj.get("behind_by").getAsInt();

            return -1;
        } catch (IOException e) {
            e.printStackTrace();
            return -3;
        }
    }

}

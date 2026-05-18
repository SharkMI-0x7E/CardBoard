/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2024 CardboardPowered.org and contributors
 * Copyright (C) 2025-2026 SharkMI and contributors
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

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.cardboardpowered.CardboardConfig;
import org.cardboardpowered.CardboardLogger;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class CardboardConsoleCommandSender implements ConsoleCommandSender, CommandSender {

    @Override
    public String getName() {
        return "CONSOLE";
    }

    @Override
    public Server getServer() {
        return Bukkit.getServer();
    }

    @Override
    public void sendMessage(String msg) {
        // BukkitLogger.getLogger().info(msg);
    	if (CardboardConfig.shouldStripConsoleColor) {
    		CardboardLogger.getSLF4J().info( ChatColor.stripColor(msg) );
    	}
    	CardboardLogger.getSLF4J().info(msg);
    }

    @Override
    public void sendMessage(String[] arg0) {
        for (String str : arg0) sendMessage(str);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin arg0, int arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2) {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2, int arg3) {
        return null;
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return null;
    }

    @Override
    public boolean hasPermission(String arg0) {
        return true;
    }

    @Override
    public boolean hasPermission(Permission arg0) {
        return true;
    }

    @Override
    public boolean isPermissionSet(String arg0) {
        return true;
    }

    @Override
    public boolean isPermissionSet(Permission arg0) {
        return true;
    }

    @Override
    public void recalculatePermissions() {
    }

    @Override
    public void removeAttachment(PermissionAttachment arg0) {
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean arg0) {
    }

    @Override
    public void abandonConversation(Conversation arg0) {
    }

    @Override
    public void abandonConversation(Conversation arg0, ConversationAbandonedEvent arg1) {
    }

    @Override
    public void acceptConversationInput(String arg0) {
    }

    @Override
    public boolean beginConversation(Conversation arg0) {
        return false;
    }

    @Override
    public boolean isConversing() {
        return false;
    }

    @Override
    public void sendRawMessage(String msg) {
        Bukkit.getLogger().info(msg);
    }

    public void sendMessage(UUID uuid, String[] msg) {
        sendMessage(msg);
    }

    public void sendMessage(UUID uuid, String msg) {
        sendMessage(msg);
    }

    public void sendRawMessage(UUID uuid, String msg) {
        sendRawMessage(msg);
    }

    private final CommandSender.Spigot spigot = new CommandSender.Spigot() {

        @Override
        public void sendMessage(BaseComponent component) {
            CardboardConsoleCommandSender.this.sendMessage(TextComponent.toLegacyText(component));
        }

        @Override
        public void sendMessage(BaseComponent... components) {
            CardboardConsoleCommandSender.this.sendMessage(TextComponent.toLegacyText(components));
        }

        @Override
        public void sendMessage(UUID sender, BaseComponent... components) {
            this.sendMessage(components);
        }

        @Override
        public void sendMessage(UUID sender, BaseComponent component) {
            this.sendMessage(component);
        }
    };

    @Override
    public org.bukkit.command.CommandSender.Spigot spigot() {
        return spigot;
    }

    @Override
    public @NotNull Component name() {
        return Component.text("Console");
    }

}

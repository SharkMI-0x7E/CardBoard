/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2024 CardboardPowered.org and contributors
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

import com.google.common.base.Joiner;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.tree.CommandNode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;

public final class MinecraftCommandWrapper extends BukkitCommand {

    private final Commands dispatcher;
    public final CommandNode<?> vanillaCommand;

    public MinecraftCommandWrapper(Commands dispatcher, CommandNode<?> vanillaCommand) {
        super(vanillaCommand.getName(), "A Minecraft provided command", vanillaCommand.getUsageText(), Collections.emptyList());
        this.dispatcher = dispatcher;
        this.vanillaCommand = vanillaCommand;
        this.setPermission(getPermission(vanillaCommand));
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) return true;

        // Lnet/minecraft/server/command/CommandManager;parseAndExecute  (Lnet/minecraft/server/command/ServerCommandSource;Ljava/lang/String;)V
        // Lnet/minecraft/server/command/CommandManager;executeWithPrefix(Lnet/minecraft/server/command/ServerCommandSource;Ljava/lang/String;)V
        
        CommandSourceStack icommandlistener = MinecraftCommandWrapper.getCommandSource(sender);
        this.dispatcher.performPrefixedCommand(icommandlistener, this.toDispatcher(args, this.getName()));
        
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) throws IllegalArgumentException {
        CommandSourceStack icommandlistener = getCommandSource(sender);
        ParseResults<CommandSourceStack> parsed = dispatcher.getDispatcher().parse(toDispatcher(args, getName()), icommandlistener);

        List<String> results = new ArrayList<>();
        dispatcher.getDispatcher().getCompletionSuggestions(parsed).thenAccept(suggestions -> suggestions.getList().forEach(s -> results.add(s.getText())));
        return results;
    }

    public static String getPermission(CommandNode<?> vanillaCommand) {
        return "minecraft.command." + ((vanillaCommand.getRedirect() == null) ? vanillaCommand.getName() : vanillaCommand.getRedirect().getName());
    }

    private String toDispatcher(String[] args, String name) {
        return name + ((args.length > 0) ? " " + Joiner.on(' ').join(args) : "");
    }

    public static CommandSourceStack getCommandSource(CommandSender s) {
        if (s instanceof CraftPlayer)
            return ((CraftPlayer)s).getHandle().createCommandSourceStack();
        if (s instanceof CraftEntity)
            return ((CraftEntity)s).getHandle().createCommandSourceStackForNameResolution( (ServerLevel) ((CraftEntity)s).getWorld() );
        if (s instanceof ConsoleCommandSender)
            return ((CraftServer) s.getServer()).getServer().createCommandSourceStack();

        return null;
    }

}

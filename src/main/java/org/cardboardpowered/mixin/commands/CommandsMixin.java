/**
 * CardboardPowered - Bukkit/Spigot for Fabric
 * Copyright (C) CardboardPowered.org and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.mixin.commands;

import com.google.common.collect.Maps;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

@Mixin(Commands.class)
public class CommandsMixin {

	// void makeTreeForSource( CommandNode tree, CommandNode result,  ServerCommandSource source, Map resultNodes) 
	// void deepCopyNodes    ( CommandNode root, CommandNode newRoot, Object source,              Map nodes) 
	
    @Shadow
    public com.mojang.brigadier.CommandDispatcher<CommandSourceStack> dispatcher;

    /*
    @Shadow
    public void makeTreeForSource(CommandNode<ServerCommandSource>a, CommandNode<CommandSource> b, ServerCommandSource c, Map<CommandNode<ServerCommandSource>, CommandNode<CommandSource>> map) {
    }
    */
    
    @Shadow
    private static <S> void fillUsableCommands(CommandNode<S> root, CommandNode<S> newRoot, S source, Map<CommandNode<S>, CommandNode<S>> nodes) {    	
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Inject(at = @At("HEAD"), method = "sendCommands")
    public void bukkitize(ServerPlayer entityplayer, CallbackInfo ci) {
        //if ( SpigotConfig.tabComplete < 0 ) return; // Spigot

        Map<CommandNode<CommandSourceStack>, CommandNode<CommandSourceStack>> map = Maps.newIdentityHashMap();
        RootCommandNode vanillaRoot = new RootCommandNode();

        RootCommandNode<CommandSourceStack> vanilla = entityplayer.level().getServer().getCommands().getDispatcher().getRoot();
        map.put(vanilla, vanillaRoot);
        this.fillUsableCommands(vanilla, vanillaRoot, entityplayer.createCommandSourceStack(), (Map) map);

        RootCommandNode<CommandSourceStack> rootcommandnode = new RootCommandNode();

        map.put(this.dispatcher.getRoot(), rootcommandnode);
        this.fillUsableCommands(this.dispatcher.getRoot(), rootcommandnode, entityplayer.createCommandSourceStack(), (Map) map);

        Collection<String> bukkit = new LinkedHashSet<>();
        for (CommandNode node : rootcommandnode.getChildren())
            bukkit.add(node.getName());

        PlayerCommandSendEvent event = new PlayerCommandSendEvent((Player) ((ServerPlayerBridge)entityplayer).getBukkitEntity(), new LinkedHashSet<>(bukkit));
        CraftEventFactory.callEvent(event);

        // Remove labels that were removed during the event
        //for (String orig : bukkit)
        //    if (!event.getCommands().contains(orig)) rootcommandnode.removeCommand(orig);
    }

    /*
    public void sendCommandTree(ServerPlayerEntity player) {
		Map<CommandNode<ServerCommandSource>, CommandNode<ServerCommandSource>> map = new HashMap();
		RootCommandNode<ServerCommandSource> rootCommandNode = new RootCommandNode<>();
		map.put(this.dispatcher.getRoot(), rootCommandNode);
		deepCopyNodes(this.dispatcher.getRoot(), rootCommandNode, player.getCommandSource(), map);
		player.networkHandler.sendPacket(new CommandTreeS2CPacket(rootCommandNode, INSPECTOR));
	}
    
    rivate void sendAsync(ServerPlayerEntity player, Collection<CommandNode<ServerCommandSource>> dispatcherRootChildren) {
        HashMap map = new HashMap();
        RootCommandNode rootCommandNode = new RootCommandNode();
        map.put((CommandNode)this.dispatcher.getRoot(), (CommandNode)rootCommandNode);
        CommandManager.fillUsableCommands(dispatcherRootChildren, rootCommandNode, player.getCommandSource(), map);
        LinkedHashSet<String> bukkit = new LinkedHashSet<String>();
        for (CommandNode node : rootCommandNode.getChildren()) {
            bukkit.add(node.getName());
        }
        new AsyncPlayerSendCommandsEvent((Player)player.getBukkitEntity(), rootCommandNode, false).callEvent();
        MinecraftServer.getServer().execute(() -> this.runSync(player, bukkit, (RootCommandNode<ServerCommandSource>)rootCommandNode));
    }

    private void runSync(ServerPlayerEntity player, Collection<String> bukkit, RootCommandNode<ServerCommandSource> rootCommandNode) {
        new AsyncPlayerSendCommandsEvent((Player)player.getBukkitEntity(), rootCommandNode, true).callEvent();
        PlayerCommandSendEvent event = new PlayerCommandSendEvent((Player)player.getBukkitEntity(), new LinkedHashSet<String>(bukkit));
        event.getPlayer().getServer().getPluginManager().callEvent((Event)event);
        for (String orig : bukkit) {
            if (event.getCommands().contains(orig)) continue;
            rootCommandNode.removeCommand(orig);
        }
        player.networkHandler.method_14364_sendPacket(new CommandTreeS2CPacket(rootCommandNode, INSPECTOR));
    }
    */

}

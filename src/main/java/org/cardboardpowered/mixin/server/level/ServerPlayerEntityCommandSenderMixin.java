/**
 * Copyright (C) 2026 SharkMI and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 */
package org.cardboardpowered.mixin.server.level;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.command.CommandSender;
import org.spongepowered.asm.mixin.Mixin;

import org.cardboardpowered.bridge.commands.CommandSourceBridge;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;

@Mixin(targets = "net/minecraft/server/level/ServerPlayer$3")
public class ServerPlayerEntityCommandSenderMixin implements CommandSourceBridge {

	/**
	 */
	@Override
    public CommandSender getBukkitSender(CommandSourceStack source) {
		// System.out.println("DEBUG: getBukkitSender!");
		
		if (source.isPlayer()) {
			ServerPlayer plr = source.getPlayer();
			return ((ServerPlayerBridge) plr) .getBukkitEntity();
		}
		
		return ((EntityBridge) source.entity).getBukkitEntity();
		
		// return ( (IMixinEntity)  ((ServerPlayerEntity) (Object) this) ) .getBukkitEntity();
    }
	
}

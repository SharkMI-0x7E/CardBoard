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
package org.cardboardpowered.mixin.commands;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.spongepowered.asm.mixin.Mixin;
import org.cardboardpowered.bridge.commands.CommandSourceBridge;

@Mixin(CommandSource.class)
public interface CommandSourceMixin extends CommandSourceBridge {

	// @Override
	// public CommandSender getBukkitSender(ServerCommandSource source);

	@Override
	default CommandSender getBukkitSender(CommandSourceStack source) {
		if (source.isPlayer()) {
			// Cardboard Note: Redirect ServerPlayerEntity$3 to ServerPlayerEntity
			return ( (CommandSourceBridge) source.getPlayer() ).getBukkitSender(source);
		}

		if (null != source.entity) {
			return ( (CommandSourceBridge) source.getEntity() ).getBukkitSender(source);
		}
			
		CommandSource output = source.source;
		
		// Memic Default Error
		String msg1 = " does not define or inherit an implementation of the resolved method 'org.bukkit.command.CommandSender";
		String msg2 = " getBukkitSender(net.minecraft.class_2168/ServerCommandSource)' of interface IMixinCommandOutput.";
		throw new AbstractMethodError(
				"Receiver class " + output.getClass().getName() + msg1 +  msg2
		);
	}

}
/**
 * Cardboard - The Bukkit for Fabric Project
 * Copyright (C) 2020-2025 Isaiah and contributors
 */
package org.cardboardpowered.bridge.commands;

import org.bukkit.command.CommandSender;
import org.cardboardpowered.mixin.commands.CommandSourceStackMixin;

/**
 * Injection Interface for ServerCommandSource.
 * 
 * @see {@link CommandSourceStackMixin}
 */
public interface CommandSourceStackBridge {

	/**
	 */
    CommandSender getBukkitSender();

}
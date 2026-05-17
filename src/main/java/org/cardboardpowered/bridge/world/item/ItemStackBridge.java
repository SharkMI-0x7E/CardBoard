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
package org.cardboardpowered.bridge.world.item;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import org.bukkit.inventory.ItemStack;

public interface ItemStackBridge {
	void cardboard$restore_patch(DataComponentPatch changes);

	public ItemStack cardboard$getBukkitStack();

	void cardboard$setItem(Item item);

	ItemStack cardboard$asBukkitMirror();

	ItemStack cardboard$asBukkitCopy();

	static net.minecraft.world.item.ItemStack fromBukkitCopy(org.bukkit.inventory.ItemStack itemstack) {
		return org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(itemstack);
	}
}

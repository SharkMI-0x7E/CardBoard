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

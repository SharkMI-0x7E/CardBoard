package org.cardboardpowered.extras;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record Entity_DefaultDrop(Item item, org.bukkit.inventory.ItemStack stack, java.util.function.@Nullable Consumer<ItemStack> dropConsumer) {
    public Entity_DefaultDrop(final ItemStack stack, final java.util.function.Consumer<ItemStack> dropConsumer) {
        this(stack.getItem(), org.bukkit.craftbukkit.inventory.CraftItemStack.asCraftMirror(stack), dropConsumer);
    }

    public void runConsumer(final java.util.function.Consumer<org.bukkit.inventory.ItemStack> fallback) {
        if (this.dropConsumer == null || org.bukkit.craftbukkit.inventory.CraftItemType.bukkitToMinecraft(this.stack.getType()) != this.item) {
            fallback.accept(this.stack);
        } else {
            this.dropConsumer.accept(org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(this.stack));
        }
    }
}
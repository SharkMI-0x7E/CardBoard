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
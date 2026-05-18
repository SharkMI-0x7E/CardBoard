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
package org.cardboardpowered.mixin.world;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import org.cardboardpowered.bridge.world.ContainerBridge;

@Mixin(CompoundContainer.class)
public abstract class CompoundContainerMixin implements Container, ContainerBridge {

    @Shadow public Container container1;
    @Shadow public Container container2;

    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();

    @Override
    public List<ItemStack> getContents() {
        List<ItemStack> result = new ArrayList<ItemStack>(this.container1.getContainerSize() + this.container2.getContainerSize());
        for (int i = 0; i < (this.container1.getContainerSize() + this.container2.getContainerSize()); i++)
            result.add(this.getItem(i));
        return result;
    }

    @Shadow
    public ItemStack getItem(int i) {
        return null;
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        this.container1.startOpen(who.getHandle());
        this.container2.startOpen(who.getHandle());
        transaction.add(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        this.container1.stopOpen(who.getHandle());
        this.container2.stopOpen(who.getHandle());
        transaction.remove(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public InventoryHolder getOwner() {
        return null; // Bukkit DoubleChest does not refer to this method.
    }

    @Override
    public void cardboard$setMaxStackSize(int size) {
        ((ContainerBridge)this.container1).cardboard$setMaxStackSize(size);
        ((ContainerBridge)this.container2).cardboard$setMaxStackSize(size);
    }

    @Override
    public Location getLocation() {
        return ((ContainerBridge)this.container1).getLocation();
    }

    @Override
    public int getMaxStackSize() {
        return MAX_STACK;
    }

}
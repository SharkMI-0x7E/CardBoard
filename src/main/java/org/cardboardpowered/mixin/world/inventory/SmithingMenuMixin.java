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
package org.cardboardpowered.mixin.world.inventory;

import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.inventory.CraftInventorySmithing;
import net.minecraft.world.inventory.SmithingMenu;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.world.inventory.ContainerLevelAccessBridge;
import org.spongepowered.asm.mixin.Mixin;

import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;

@Mixin(SmithingMenu.class)
public class SmithingMenuMixin extends ItemCombinerMenuMixin {

    private CraftInventoryView bukkitEntity;

    // CraftBukkit start
    @Override
    public org.bukkit.craftbukkit.inventory.CraftInventoryView getBukkitView() {
        if (this.bukkitEntity != null) {
            return this.bukkitEntity;
        }

        org.bukkit.craftbukkit.inventory.CraftInventory inventory = new org.bukkit.craftbukkit.inventory.CraftInventorySmithing(
                ((ContainerLevelAccessBridge)this.access).getLocation(), this.inputSlots, this.resultSlots);
        this.bukkitEntity = new org.bukkit.craftbukkit.inventory.CraftInventoryView((HumanEntity) ((EntityBridge)this.player).getBukkitEntity(), inventory, (SmithingMenu)(Object)this);
        return this.bukkitEntity;
    }
    // CraftBukkit end

}
/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2024 CardboardPowered.org and contributors
 * Copyright (C) 2025-2026 SharkMI and contributors
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
package org.cardboardpowered.mixin.world.inventory;

import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import org.bukkit.entity.Player;
import org.bukkit.craftbukkit.inventory.CraftInventoryAnvil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import org.cardboardpowered.bridge.world.inventory.AnvilMenuBridge;
import org.cardboardpowered.bridge.world.inventory.ContainerLevelAccessBridge;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin extends ItemCombinerMenuMixin implements AnvilMenuBridge {

    // TODO Add AnvilPrepareEvent

    public int maximumRepairCost_BF = 40;
    public CraftInventoryView bukkitEntity;

    @Shadow public String itemName;
    @Shadow public DataSlot cost;

    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null)
            return bukkitEntity;

        org.bukkit.craftbukkit.inventory.CraftInventory inventory = new CraftInventoryAnvil(
                ((ContainerLevelAccessBridge)access).getLocation(), this.inputSlots, this.resultSlots);
        bukkitEntity = new CraftInventoryView((Player)((ServerPlayerBridge)this.player).getBukkitEntity(), inventory, (AnvilMenu)(Object)this);
        return bukkitEntity;
    }

    @Override
    public String getNewItemName_BF() {
        return itemName;
    }

    @Override
    public int getLevelCost_BF() {
        return cost.get();
    }

    @Override
    public void setLevelCost_BF(int i) {
        cost.set(i);
    }

    @Override
    public int getMaxRepairCost_BF() {
        return maximumRepairCost_BF;
    }

    @Override
    public void setMaxRepairCost_BF(int levels) {
        maximumRepairCost_BF = levels;
    }

}
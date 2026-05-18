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
package org.cardboardpowered.extras;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

public class BukkitChestDoubleInventory implements MenuProvider {
    public final net.minecraft.world.CompoundContainer inventory;
    private final ChestBlockEntity leftChest;
    private final ChestBlockEntity rightChest;

    public BukkitChestDoubleInventory(ChestBlockEntity leftChest, ChestBlockEntity rightChest,
                                      net.minecraft.world.CompoundContainer inventory) {
        this.leftChest = leftChest;
        this.rightChest = rightChest;
        this.inventory = inventory;
    }

    @Override
    public Component getDisplayName() {
        return this.leftChest.hasCustomName() ? this.leftChest.getDisplayName() :
                (this.rightChest.hasCustomName() ? this.rightChest.getDisplayName() :
                        Component.translatable("container.chestDouble"));
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        if (this.leftChest.canOpen(player) && this.rightChest.canOpen(player)) {
            this.leftChest.unpackLootTable(inv.player);
            this.rightChest.unpackLootTable(inv.player);
            return ChestMenu.sixRows(syncId, inv, this.inventory);
        } else {
            return null;
        }
    }
}

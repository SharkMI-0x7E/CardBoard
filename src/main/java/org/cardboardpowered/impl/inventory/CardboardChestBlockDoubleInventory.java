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
package org.cardboardpowered.impl.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

public class CardboardChestBlockDoubleInventory implements MenuProvider {

    private final ChestBlockEntity tileentitychest;
    private final ChestBlockEntity tileentitychest1;
    public final net.minecraft.world.CompoundContainer inventorylargechest;

    public CardboardChestBlockDoubleInventory(ChestBlockEntity tileentitychest, ChestBlockEntity tileentitychest1, net.minecraft.world.CompoundContainer inventorylargechest) {
        this.tileentitychest = tileentitychest;
        this.tileentitychest1 = tileentitychest1;
        this.inventorylargechest = inventorylargechest;
    }

    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerinventory, Player entityhuman) {
        if (tileentitychest.canOpen(entityhuman) && tileentitychest1.canOpen(entityhuman)) {
            tileentitychest.unpackLootTable(playerinventory.player);
            tileentitychest1.unpackLootTable(playerinventory.player);
            return ChestMenu.sixRows(i, playerinventory, inventorylargechest);
        } else return null;
    }

    @Override
    public Component getDisplayName() {
        return (Component) (tileentitychest.hasCustomName() ? tileentitychest.getDisplayName() : (tileentitychest1.hasCustomName() ? tileentitychest1.getDisplayName() : Component.translatable("container.chestDouble")));
    }

}

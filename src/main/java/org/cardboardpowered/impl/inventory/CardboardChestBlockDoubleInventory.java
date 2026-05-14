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

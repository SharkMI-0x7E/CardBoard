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

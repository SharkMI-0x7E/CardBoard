package org.bukkit.craftbukkit.inventory.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;

public interface CraftMenuBuilder {

    public AbstractContainerMenu build(ServerPlayer var1, MenuType<?> var2);

    public static CraftMenuBuilder worldAccess(LocationBoundContainerBuilder builder) {
        return (player, type) -> builder.build(((ServerPlayerBridge)player).cardboard$nextContainerCounter(), player.getInventory(), ContainerLevelAccess.create(player.level(), player.blockPosition()));
    }

    public static CraftMenuBuilder tileEntity(TileEntityObjectBuilder objectBuilder, Block block) {
        return (player, type) -> objectBuilder.build(player.blockPosition(), block.defaultBlockState()).createMenu(((ServerPlayerBridge)player).cardboard$nextContainerCounter(), player.getInventory(), player);
    }

    public static interface LocationBoundContainerBuilder {
        public AbstractContainerMenu build(int var1, Inventory var2, ContainerLevelAccess var3);
    }

    public static interface TileEntityObjectBuilder {
        public MenuProvider build(BlockPos var1, BlockState var2);
    }

}
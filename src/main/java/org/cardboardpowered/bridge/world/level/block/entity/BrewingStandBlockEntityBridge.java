package org.cardboardpowered.bridge.world.level.block.entity;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface BrewingStandBlockEntityBridge {

    public NonNullList<ItemStack> cardboard_getInventory();

}
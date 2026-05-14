/**
 * CardboardPowered - Bukkit/Spigot for Fabric
 * Copyright (C) CardboardPowered.org and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 */
package org.cardboardpowered.mixin.world.item.enchantment;

import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;

import org.bukkit.craftbukkit.event.CraftEventFactory;

/**
 */
/// @Mixin(value = FrostWalkerEnchantment.class, priority = 999)
@Mixin(Enchantments.class) // TODO
public class EnchantmentsMixin {

    /**
     * @reason BlockFormEvent - Add call to {@link CraftEventFactory#handleBlockFormEvent}
     * @author .
     * 
     * @param entity - The entity/player
     * @param world  - the world the entity is in.
     * @param pos    - The current {@link BlockPos}
     */
	/*
    @Overwrite
    public static void freezeWater(LivingEntity living, World worldIn, BlockPos pos, int level) {
        if (living.isOnGround()) {
            BlockState blockstate = Blocks.FROSTED_ICE.getDefaultState();
            int f = Math.min(16, 2 + level);
            BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

            for (BlockPos blockpos : BlockPos.iterate(pos.add(-f, -1, -f), pos.add(f, -1, f))) {
                if (blockpos.isWithinDistance(living.getPos(), f)) {
                    blockpos$mutable.set(blockpos.getX(), blockpos.getY() + 1, blockpos.getZ());
                    BlockState blockstate1 = worldIn.getBlockState(blockpos$mutable);
                    if (blockstate1.isAir()) {
                        BlockState blockstate2 = worldIn.getBlockState(blockpos);
                        boolean isFull = blockstate2.getBlock() == Blocks.WATER && blockstate2.get(FluidBlock.LEVEL) == 0;
                        if (blockstate2.isLiquid() && isFull && blockstate.canPlaceAt(worldIn, blockpos) && worldIn.canPlace(blockstate, blockpos, ShapeContext.absent())) {
                            if (CraftEventFactory.handleBlockFormEvent(worldIn, blockpos, blockstate, living)) {
                                worldIn.scheduleBlockTick(blockpos, Blocks.FROSTED_ICE, MathHelper.nextInt(living.getRandom(), 60, 120));
                            }
                        }
                    }
                }
            }
        }
    }
    */

}
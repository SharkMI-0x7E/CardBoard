/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors*
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
package org.cardboardpowered.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// import java.util.Random;

@Mixin(BambooStalkBlock.class)
public class BambooStalkBlockMixin extends Block {

    @Shadow @Final public static EnumProperty<BambooLeaves> LEAVES;
    @Shadow @Final public static IntegerProperty AGE;

    @Shadow @Final public static IntegerProperty STAGE;

    public BambooStalkBlockMixin(Properties settings) {
        super(settings);
    }

    @Redirect(method = "performBonemeal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"))
    private <T extends Comparable<T>> T bukkitSkipIfCancel(BlockState state, Property<T> property) {
        if (!state.is(Blocks.BAMBOO)) {
            return (T) Integer.valueOf(1);
        } else {
            return state.getValue(property);
        }
    }

    /**
     * @author Cardboard
     * @reason Fire BlockSpreadEvent for bamboo growth
     *
     * TODO: Cannot replace with @Inject - this @Overwrite rewrites the growBamboo method
     * to use CraftEventFactory.handleBlockSpreadEvent for each bamboo block placement,
     * allowing event cancellation to prevent bamboo growth. The original method's growth
     * logic is preserved but wrapped in event checks.
     */
    @Overwrite
    public void growBamboo(BlockState state, Level world, BlockPos pos, RandomSource random, int height) {
        BlockState blockState = world.getBlockState(pos.below());
        BlockPos blockPos = pos.below(2);
        BlockState blockState2 = world.getBlockState(blockPos);
        BambooLeaves bambooLeaves = BambooLeaves.NONE;
        boolean shouldUpdateOthers = false;
        if (height >= 1) {
            if (blockState.is(Blocks.BAMBOO) && blockState.getValue(LEAVES) != BambooLeaves.NONE) {
                if (blockState.is(Blocks.BAMBOO) && blockState.getValue(LEAVES) != BambooLeaves.NONE) {
                    bambooLeaves = BambooLeaves.LARGE;
                    if (blockState2.is(Blocks.BAMBOO)) {
                        shouldUpdateOthers = true;
                    }
                }
            } else {
                bambooLeaves = BambooLeaves.SMALL;
            }
        }
        int i = (Integer)state.getValue(AGE) != 1 && !blockState2.is(Blocks.BAMBOO) ? 0 : 1;
        int j = (height < 11 || !(random.nextFloat() < 0.25F)) && height != 15 ? 0 : 1;
        if (CraftEventFactory.handleBlockSpreadEvent(world, pos, pos.above(), this.defaultBlockState().setValue(AGE, i).setValue(LEAVES, bambooLeaves).setValue(STAGE, j), 3)) {
            if (shouldUpdateOthers) {
                world.setBlock(pos.below(), blockState.setValue(BambooStalkBlock.LEAVES, BambooLeaves.SMALL), 3);
                world.setBlock(blockPos, blockState2.setValue(BambooStalkBlock.LEAVES, BambooLeaves.NONE), 3);
            }
        }
    }
}

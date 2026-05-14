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
     * @author
     * @reason
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

package io.papermc.paper;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.Strategy;

public final class FeatureHooks {
    public static PalettedContainer<BlockState> emptyPalettedBlockContainer() {
        return new PalettedContainer<>(Blocks.AIR.defaultBlockState(), Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY)); // Paper - Anti-Xray - Add preset block states
    }
}

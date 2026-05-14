package org.cardboardpowered.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BambooSaplingBlock.class)
public class BambooSaplingBlockMixin {

    public boolean bukkitSpreadEvent(Level world, BlockPos pos, BlockState newState, int flags) {
        return CraftEventFactory.handleBlockSpreadEvent(world, pos.below(), pos, newState, flags);
    }
}

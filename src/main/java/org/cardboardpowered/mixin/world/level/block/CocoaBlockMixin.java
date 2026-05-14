package org.cardboardpowered.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@MixinInfo(events = {"BlockGrowEvent"})
@Mixin (CocoaBlock.class)
public class CocoaBlockMixin {

    @Redirect (method = "randomTick", at = @At (value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean bukkit_grow0(ServerLevel world, BlockPos pos, BlockState state, int flags) {
        return CraftEventFactory.handleBlockGrowEvent(world, pos, state, flags);
    }

    @Redirect (method = "performBonemeal", at = @At (value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean bukkit_grow1(ServerLevel world, BlockPos pos, BlockState state, int flags) {
        return CraftEventFactory.handleBlockGrowEvent(world, pos, state, flags);
    }
}

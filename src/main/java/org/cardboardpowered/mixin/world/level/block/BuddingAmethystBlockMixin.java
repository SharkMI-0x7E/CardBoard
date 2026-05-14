package org.cardboardpowered.mixin.world.level.block;

import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BuddingAmethystBlock.class)
public class BuddingAmethystBlockMixin {

    private AtomicReference<BlockPos> fromPos = new AtomicReference<>();

    @Inject(method = "randomTick", at = @At("HEAD"))
    private void getFromPos(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        fromPos.set(pos);
    }
    @Redirect(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean blockSpread(ServerLevel instance, BlockPos blockPos, BlockState blockState) {
        return CraftEventFactory.handleBlockSpreadEvent(instance,fromPos.get(), blockPos, blockState, 3);
    }
}

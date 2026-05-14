package org.cardboardpowered.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WaterlilyBlock.class)
public class WaterlilyBlockMixin {

	// TODO: EntityInsideBlockEvent
	
	/*
    @Inject(
    		method = "onEntityCollision", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;breakBlock(Lnet/minecraft/util/math/BlockPos;ZLnet/minecraft/entity/Entity;)Z"),
    		require = 0 
    )
    public void entityChangeBlock_1_21_9(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler ech, CallbackInfo ci) {
        if (CraftEventFactory.callEntityChangeBlockEvent(entity, pos, Blocks.AIR.getDefaultState()).isCancelled()) {
            ci.cancel();
        }
    }
    */
    
    @Inject(
    		method = "entityInside", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;)Z")
    )
    public void entityChangeBlock_1_21_10(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier ech, boolean b, CallbackInfo ci) {
        // CraftBukkit start
        if (!org.bukkit.craftbukkit.event.CraftEventFactory.callEntityChangeBlockEvent(entity, pos, state.getFluidState().createLegacyBlock())) { // Paper - fix wrong block state
            ci.cancel();
            return;
        }
        // CraftBukkit end
    }
}

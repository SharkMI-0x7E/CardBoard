package org.cardboardpowered.mixin.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = MinecartItem.class, priority = 999)
public class MinecartItemMixin {

    @Redirect(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean cardboard$minecart_redirect_vanilla_spawnEntity(ServerLevel instance, Entity entity) {
        return false;
    }

    @Inject(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void cardboard$minecart_entity_place_event(UseOnContext useOnContext, CallbackInfoReturnable<InteractionResult> cir,
                                    Level level, BlockPos blockPos, BlockState blockState, ItemStack itemStack,
                                    RailShape railShape, double d, Vec3 vec,
                                    AbstractMinecart abstractMinecart, ServerLevel serverLevel) {
        // CraftBukkit start
        if (CraftEventFactory.callEntityPlaceEvent(useOnContext, abstractMinecart).isCancelled()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
        // CraftBukkit end
        if (!level.addFreshEntity(abstractMinecart)) cir.setReturnValue(InteractionResult.PASS); // CraftBukkit
    }
	
}

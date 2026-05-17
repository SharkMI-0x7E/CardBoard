/**
 * Copyright (C) 2026 SharkMI and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 */
package org.cardboardpowered.mixin.world.item;

import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;

@MixinInfo(events = {"BlockIgniteEvent"})
@Mixin(FireChargeItem.class)
public class FireChargeItemMixin {

    @Inject(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/FireChargeItem;playSound(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    public void cardboard$fireChargeItem_BlockIgniteEvent(UseOnContext context, CallbackInfoReturnable<InteractionResult> ci) {
        Level world = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockState state = world.getBlockState(blockpos);

        if (!CampfireBlock.canLight(state))
            blockpos = blockpos.relative(context.getClickedFace());

        if (CraftEventFactory.callBlockIgniteEvent(world, blockpos, org.bukkit.event.block.BlockIgniteEvent.IgniteCause.FIREBALL, context.getPlayer()).isCancelled()) {
            if (!context.getPlayer().abilities.instabuild)
                context.getItemInHand().shrink(1);
            ci.setReturnValue(InteractionResult.PASS);
        }
    }

}
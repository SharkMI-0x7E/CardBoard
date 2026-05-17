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

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.EndCrystalItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.bukkit.craftbukkit.event.CraftEventFactory;

@Mixin(value = EndCrystalItem.class, priority = 900)
public class EndCrystalItemMixin {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void cardboard$onUseOn(UseOnContext itemactioncontext, CallbackInfoReturnable<InteractionResult> ci) {
        Level world = itemactioncontext.getLevel();
        BlockPos blockpos = itemactioncontext.getClickedPos();
        BlockState iblockdata = world.getBlockState(blockpos);

        if (!iblockdata.is(Blocks.BEDROCK) && !iblockdata.is(Blocks.OBSIDIAN)) {
            return;
        }

        BlockPos blockpos1 = blockpos.above();
        if (!world.isEmptyBlock(blockpos1)) {
            return;
        }
        double x = (double) blockpos1.getX();
        double y = (double) blockpos1.getY();
        double z = (double) blockpos1.getZ();
        List<Entity> list = world.getEntities((Entity) null, new AABB(x, y, z, x + 1.0D, y + 2.0D, z + 1.0D));

        if (!list.isEmpty()) {
            return;
        }

        if (!(world instanceof ServerLevel serverlevel)) {
            return;
        }

        EndCrystal entityendercrystal = new EndCrystal(world, x + 0.5D, y, z + 0.5D);
        entityendercrystal.setShowBottom(false);

        if (CraftEventFactory.callEntityPlaceEvent(itemactioncontext, entityendercrystal).isCancelled()) {
            ci.setReturnValue(InteractionResult.FAIL);
            ci.cancel();
            return;
        }

        world.addFreshEntity(entityendercrystal);
        EndDragonFight enderdragonbattle = serverlevel.getDragonFight();
        if (enderdragonbattle != null) {
            enderdragonbattle.tryRespawn();
        }
        itemactioncontext.getItemInHand().shrink(1);

        ci.setReturnValue(InteractionResult.SUCCESS);
        ci.cancel();
    }

}

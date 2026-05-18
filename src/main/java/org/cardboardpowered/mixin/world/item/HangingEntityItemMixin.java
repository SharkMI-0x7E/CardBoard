/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors
 *
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
package org.cardboardpowered.mixin.world.item;

import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@MixinInfo(events = {"HangingPlaceEvent"})
@Mixin(value = HangingEntityItem.class, priority = 900)
public class HangingEntityItemMixin {

    @Inject(method = "useOn", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/HangingEntity;playPlacementSound()V"))
    private void bukkitUseOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir, BlockPos blockPos, Direction direction, BlockPos blockPos2, net.minecraft.world.entity.player.Player playerEntity, ItemStack itemStack, Level world, HangingEntity abstractDecorationEntity) {
        Player who = (context.getPlayer() == null) ? null : (Player) ((ServerPlayerBridge) context.getPlayer()).getBukkitEntity();
        org.bukkit.block.Block blockClicked = CraftBlock.at((ServerLevel) world, blockPos);
        org.bukkit.block.BlockFace blockFace = CraftBlock.notchToBlockFace(direction);

        HangingPlaceEvent event = new HangingPlaceEvent((Hanging) ((EntityBridge) abstractDecorationEntity).getBukkitEntity(), who, blockClicked, blockFace, EquipmentSlot.HAND, CraftItemStack.asBukkitCopy(itemStack));
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

}
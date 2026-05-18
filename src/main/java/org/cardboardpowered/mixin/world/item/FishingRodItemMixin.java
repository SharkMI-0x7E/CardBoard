/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2024 CardboardPowered.org and contributors
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

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerFishEvent;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.cardboardpowered.bridge.world.entity.EntityBridge;

@MixinInfo(events = {"PlayerFishEvent"})
@Mixin(FishingRodItem.class)
public class FishingRodItemMixin {

    @Inject(at = @At("HEAD"), method = "use", cancellable = true)
    public void cardboard$fishingRodUse_PlayerFishEvent(Level world, Player entityhuman, InteractionHand enumhand, CallbackInfoReturnable<InteractionResult> ci) {
        if (null == entityhuman.fishing) {
            ItemStack itemstack = entityhuman.getItemInHand(enumhand);

            int i = (int)(EnchantmentHelper.getFishingTimeReduction((ServerLevel) world, itemstack, entityhuman) * 20.0f);
            int j = EnchantmentHelper.getFishingLuckBonus((ServerLevel) world, itemstack, entityhuman);
            
            FishingHook entityfishinghook = new FishingHook(entityhuman, world, j, i);
            PlayerFishEvent playerFishEvent = new PlayerFishEvent((org.bukkit.entity.Player) ((EntityBridge)entityhuman).getBukkitEntity(), null, (org.bukkit.entity.FishHook) ((EntityBridge)entityfishinghook).getBukkitEntity(), PlayerFishEvent.State.FISHING);
            Bukkit.getPluginManager().callEvent(playerFishEvent);
    
            if (playerFishEvent.isCancelled()) {
                entityhuman.fishing = null;
                ci.setReturnValue( InteractionResult.PASS );
                return;
            }
            world.addFreshEntity(entityfishinghook); 
            ci.setReturnValue( InteractionResult.SUCCESS );
            return;
        }
    }

}
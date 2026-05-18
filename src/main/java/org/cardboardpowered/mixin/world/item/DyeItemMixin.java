/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors*
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

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.cardboardpowered.bridge.world.entity.EntityBridge;

@MixinInfo(events = {"SheepDyeWoolEvent"})
@Mixin(value = DyeItem.class, priority = 900)
public class DyeItemMixin {

    @Shadow
    public DyeColor dyeColor;

    @SuppressWarnings("deprecation")
    @Inject(method = "interactLivingEntity", at = @At("HEAD"), cancellable = true)
    private void cardboard$onInteractLivingEntity(ItemStack itemstack, Player entityhuman, LivingEntity entityliving, InteractionHand enumhand, CallbackInfoReturnable<InteractionResult> ci) {
        if (!(entityliving instanceof Sheep entitysheep)) {
            return;
        }

        if (entitysheep.isAlive() && !entitysheep.isSheared() && entitysheep.getColor() != this.dyeColor) {
            if (!entityhuman.level().isClientSide()) {
                byte bColor = (byte) this.dyeColor.getId();
                SheepDyeWoolEvent event = new SheepDyeWoolEvent(
                    (org.bukkit.entity.Sheep) ((EntityBridge) entitysheep).getBukkitEntity(),
                    org.bukkit.DyeColor.getByWoolData(bColor)
                );
                Bukkit.getServer().getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    ci.setReturnValue(InteractionResult.PASS);
                    ci.cancel();
                    return;
                }

                // 如果事件修改了颜色，更新dyeColor字段让原始方法使用
                DyeColor newColor = DyeColor.byId((byte) event.getColor().getWoolData());
                if (newColor != this.dyeColor) {
                    this.dyeColor = newColor;
                }
            }
        }
    }

}

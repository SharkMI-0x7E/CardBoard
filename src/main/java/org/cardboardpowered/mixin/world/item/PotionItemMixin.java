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

import net.minecraft.world.item.PotionItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = PotionItem.class, priority = 900)
public class PotionItemMixin {

	/*
    @Inject(
    		method = "finishUsing",
    		at = @At(
    				value = "INVOKE",
    				target = "Lnet/minecraft/component/type/PotionContentsComponent;forEachEffect(Ljava/util/function/Consumer;)V"
    			)
    	)
    public void cardboard$potionitem_set_effect_event_cause(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        ((IMixinLivingEntity) user).pushEffectCause(EntityPotionEffectEvent.Cause.POTION_DRINK);
    }
    */

}
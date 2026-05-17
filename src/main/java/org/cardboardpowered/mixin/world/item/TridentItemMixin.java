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

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.cardboardpowered.bridge.world.entity.EntityBridge;

@MixinInfo(events = {"PlayerRiptideEvent"})
@Mixin(TridentItem.class)
public class TridentItemMixin {

    @Inject(at =
    		@At(
    				value = "INVOKE",
    				target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"
    		),
    		method =
    		"releaseUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)Z")
    public void doBukkitEvent_PlayerRiptideEvent(ItemStack itemstack, Level world, LivingEntity entity, int i, CallbackInfoReturnable<Boolean> ci) {
        float k = EnchantmentHelper.getTridentSpinAttackStrength(itemstack, entity);
        if (k > 0.0f) {
            PlayerRiptideEvent event = new PlayerRiptideEvent((Player)((EntityBridge)entity).getBukkitEntity(), CraftItemStack.asCraftMirror(itemstack));
            event.getPlayer().getServer().getPluginManager().callEvent(event);
        }
    }

}
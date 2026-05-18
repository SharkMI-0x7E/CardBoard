/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2024 CardboardPowered.org and contributors
 * Copyright (C) 2025-2026 SharkMI and contributors
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
package org.cardboardpowered.mixin.world.entity.projectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.cardboardpowered.api.event.CardboardFireworkExplodeEvent;
import org.cardboardpowered.mixin.world.entity.EntityMixin;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@MixinInfo(events = {"FireworkExplodeEvent", "CardboradFireworkExplodeEvent"})
@Mixin(FireworkRocketEntity.class)
public class FireworkRocketEntityMixin extends EntityMixin {

    @Inject(method = "tick", cancellable = true, at = @At("HEAD"))
    private void bukkitFireworksExplode(CallbackInfo ci) {
        InteractionResult result = CardboardFireworkExplodeEvent.EVENT.invoker().interact((FireworkRocketEntity) (Object) this);

        if (result == InteractionResult.FAIL) {
            ci.cancel();
        }
    }

    @Inject(method = "dealExplosionDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private void bukkitDamageSource(ServerLevel world, CallbackInfo ci) {
        CraftEventFactory.entityDamage = (FireworkRocketEntity) (Object) this;
    }

    @Inject(method = "dealExplosionDamage", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/LivingEntity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private void bukkitDamageSourceReset(ServerLevel world, CallbackInfo ci) {
        CraftEventFactory.entityDamage = null;
    }
}

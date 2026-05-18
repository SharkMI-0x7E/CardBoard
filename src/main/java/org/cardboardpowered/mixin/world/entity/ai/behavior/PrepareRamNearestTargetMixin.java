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
package org.cardboardpowered.mixin.world.entity.ai.behavior;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.PrepareRamNearestTarget;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PrepareRamNearestTarget.class)
public class PrepareRamNearestTargetMixin {

    @Inject(method = "method_36270",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/behavior/PrepareRamNearestTarget;chooseRamPosition(Lnet/minecraft/world/entity/PathfinderMob;Lnet/minecraft/world/entity/LivingEntity;)V"), cancellable = true)
    private void targetEvent(PathfinderMob pathAwareEntity, LivingEntity mob, CallbackInfo ci) {
        // CraftBukkit start
        EntityTargetEvent event = CraftEventFactory.callEntityTargetLivingEvent(pathAwareEntity, mob, (mob instanceof ServerPlayer) ? EntityTargetEvent.TargetReason.CLOSEST_PLAYER : EntityTargetEvent.TargetReason.CLOSEST_ENTITY);
        if (event.isCancelled() || event.getTarget() == null) {
            ci.cancel();
        }
        mob = ((CraftLivingEntity) event.getTarget()).getHandle();
        // CraftBukkit end
    }
}

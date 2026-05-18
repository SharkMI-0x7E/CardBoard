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
package org.cardboardpowered.mixin.world.entity.ambient;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Bat;
import org.bukkit.event.entity.BatToggleSleepEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import org.cardboardpowered.bridge.world.entity.EntityBridge;

@Mixin(net.minecraft.world.entity.ambient.Bat.class)
public class BatMixin {

    
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ambient/Bat;setResting(Z)V"),
            method = "customServerAiStep")
    public void mobTick_doBatSleepEvent(net.minecraft.world.entity.ambient.Bat bat, boolean sleep) {
        if (handleBatToggleSleepEvent((net.minecraft.world.entity.ambient.Bat)(Object)this, !sleep)) {
            this.setResting(sleep);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ambient/Bat;setResting(Z)V"),
            method = "hurtServer")
    public void damage_doBatSleepEvent(net.minecraft.world.entity.ambient.Bat bat, boolean sleep, ServerLevel world, DamageSource source, float amount) {
        if (handleBatToggleSleepEvent((net.minecraft.world.entity.ambient.Bat)(Object)this, true)) {
            this.setResting(false);
        }
    }

    @Shadow
    public void setResting(boolean b) {}

    // note: 1.21.4: awake is always == true.
    private static boolean handleBatToggleSleepEvent(Entity bat, boolean awake) {
        BatToggleSleepEvent event = new BatToggleSleepEvent((Bat) ((EntityBridge)bat).getBukkitEntity(), awake);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

}
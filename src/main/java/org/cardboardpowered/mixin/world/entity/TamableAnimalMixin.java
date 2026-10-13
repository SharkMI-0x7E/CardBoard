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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.mixin.world.entity;

import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityTameEvent;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@MixinInfo(events = {"EntityTameEvent"})
@Mixin(TamableAnimal.class)
public class TamableAnimalMixin {

    // Fired before any tame state is written (setTame/setOwner run inside tame()).
    // Cancelling the event skips taming entirely, matching Bukkit/Paper semantics.
    @Inject(method = "tame", at = @At("HEAD"), cancellable = true)
    private void cardboard$fireEntityTameEvent(Player player, CallbackInfo ci) {
        if (EntityTameEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return;
        }
        LivingEntity tameable = (LivingEntity) ((EntityBridge) (Object) this).getBukkitEntity();
        AnimalTamer owner = (AnimalTamer) ((EntityBridge) player).getBukkitEntity();
        EntityTameEvent event = new EntityTameEvent(tameable, owner);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}

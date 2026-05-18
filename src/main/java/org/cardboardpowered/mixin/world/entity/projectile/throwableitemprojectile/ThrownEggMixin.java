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
package org.cardboardpowered.mixin.world.entity.projectile.throwableitemprojectile;

import java.util.Random;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.cardboardpowered.impl.world.CraftWorld;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.destroystokyo.paper.event.entity.ThrownEggHatchEvent;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.world.level.LevelBridge;

@MixinInfo(events = {"ThrownEggHatchEvent", "PlayerEggThrowEvent"})
@Mixin(value = ThrownEgg.class, priority = 999)
public abstract class ThrownEggMixin {

    private final Random random = new Random();

    @Inject(at = @At(shift = Shift.AFTER, value = "HEAD"), method = "onHit", cancellable = true)
    public void cardboard_doEggThrowEvent(HitResult res, CallbackInfo ci) {
        ThrownEgg egg = (ThrownEgg)(Object)this;
        Level world = egg.level();

        if (!world.isClientSide()) {
            boolean hatching = this.random.nextInt(8) == 0; // Spigot
            byte b0 = 1;
            if (this.random.nextInt(32) == 0) b0 = 4;

            // Spigot start
            if (!hatching) b0 = 0;
            EntityType hatchingType = EntityType.CHICKEN;

            Entity shooter = egg.getOwner();
            if (shooter instanceof ServerPlayer) {
                PlayerEggThrowEvent event = new PlayerEggThrowEvent((Player) ((EntityBridge)shooter).getBukkitEntity(), (org.bukkit.entity.Egg) ((EntityBridge)egg).getBukkitEntity(), hatching, b0, hatchingType);
                CraftServer.INSTANCE.getPluginManager().callEvent(event);

                b0 = event.getNumHatches();
                hatching = event.isHatching();
                hatchingType = event.getHatchingType();
            }

            // Paper start
            ThrownEggHatchEvent event = new ThrownEggHatchEvent((org.bukkit.entity.Egg) ((EntityBridge)egg).getBukkitEntity(), hatching, b0, hatchingType);
            event.callEvent();

            b0 = event.getNumHatches();
            hatching = event.isHatching();
            hatchingType = event.getHatchingType();
            // Paper end
            if (hatching) {
                for (int i = 0; i < b0; ++i) {
                    CraftWorld cw = ((LevelBridge)world).cardboard$getWorld();
                    Entity entity = cw.createEntity_Old(new org.bukkit.Location(cw, egg.getX(), egg.getY(), egg.getZ(), egg.getYRot(), 0.0F), hatchingType.getEntityClass());
                    if (((EntityBridge)entity).getBukkitEntity() instanceof Ageable)
                        ((Ageable) ((EntityBridge)entity).getBukkitEntity()).setBaby();
                    cw.addEntity(entity, SpawnReason.EGG);
                }
            }
            // Spigot end

            world.broadcastEntityEvent(egg, (byte) 3);
            egg.discard();
        }
        ci.cancel();
        return;
    }

}
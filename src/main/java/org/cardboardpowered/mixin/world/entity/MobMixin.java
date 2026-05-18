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
package org.cardboardpowered.mixin.world.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.bukkit.event.entity.EntityTargetEvent;
import org.cardboardpowered.bridge.world.entity.MobBridge;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.server.level.ServerLevelBridge;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity implements MobBridge, EntityBridge {
    @Shadow
    @Nullable
    public LivingEntity target;

    @Shadow
    public abstract @Nullable LivingEntity getTarget();

    protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    public void setTargetCraftBukkit(LivingEntity livingEntity, CallbackInfo ci) {
        // CraftBukkit start - fire event
        boolean set = this.cardboard$setTarget(target, EntityTargetEvent.TargetReason.UNKNOWN);
        if (set) { // Let the other mods call their @Inject if set is false.
            ci.cancel();
        }
    }

    @Override
    public boolean cardboard$setTarget(@Nullable LivingEntity target, EntityTargetEvent.@Nullable TargetReason reason) {
        if (this.getTarget() == target) {
            return false;
        }
        if (reason != null) {
            if (reason == EntityTargetEvent.TargetReason.UNKNOWN && this.getTarget() != null && target == null) {
                reason = this.getTarget().isAlive() ? EntityTargetEvent.TargetReason.FORGOT_TARGET : EntityTargetEvent.TargetReason.TARGET_DIED;
            }
            if (reason == EntityTargetEvent.TargetReason.UNKNOWN) {
                ((ServerLevelBridge)this.level()).getCraftServer().getLogger().log(java.util.logging.Level.WARNING, "Unknown target reason, please report on the issue tracker", new Exception());
            }
            CraftLivingEntity ctarget = null;
            if (target != null) {
                ctarget = (CraftLivingEntity) target.getBukkitEntity();
            }
            org.bukkit.event.entity.EntityTargetLivingEntityEvent event = new org.bukkit.event.entity.EntityTargetLivingEntityEvent(this.getBukkitEntity(), ctarget, reason);
            if (!event.callEvent()) {
                return false;
            }

            if (event.getTarget() != null) {
                target = ((CraftLivingEntity) event.getTarget()).getHandle();
            } else {
                target = null;
            }
        }
        this.target = target;
        return true;
        // CraftBukkit end
    }
}

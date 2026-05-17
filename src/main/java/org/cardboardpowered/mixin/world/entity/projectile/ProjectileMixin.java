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
package org.cardboardpowered.mixin.world.entity.projectile;

import org.bukkit.projectiles.ProjectileSource;
import org.cardboardpowered.mixin.world.entity.EntityMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.cardboardpowered.bridge.world.entity.EntityBridge;

@Mixin(Projectile.class)
public class ProjectileMixin extends EntityMixin {

    @Inject(at = @At("TAIL"), method = "setOwner")
    public void setProjectileSource(EntityReference<Entity> entity, CallbackInfo ci) {
    	cb$refreshProjectileSource(false);
    }

    @Inject(at = @At("HEAD"), method = "onHit")
    public void fireProjectileHitEvent(HitResult hitResult, CallbackInfo ci) {
        CraftEventFactory.callProjectileHitEvent((Projectile)(Object)this, hitResult);
    }

    @Shadow
    public void onHitBlock(BlockHitResult blockHitResult) {
    }
    
    private void cb$refreshProjectileSource(boolean fillCache) {
        CraftEntity craftEntity;
        Entity owner;
        if (fillCache) {
            this.getOwner();
        }
        if ((owner = this.getOwner()) != null && this.projectileSource == null && (craftEntity = ((EntityBridge)owner).getBukkitEntity()) instanceof ProjectileSource) {
            ProjectileSource source = (ProjectileSource)craftEntity;
            this.projectileSource = source;
        }
    }
    
    @Shadow
    public Entity getOwner() {
        return null; // Shadowed
    }

}
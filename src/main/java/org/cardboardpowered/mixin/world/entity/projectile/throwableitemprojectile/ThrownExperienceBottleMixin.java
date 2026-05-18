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
package org.cardboardpowered.mixin.world.entity.projectile.throwableitemprojectile;

import org.cardboardpowered.mixin.world.entity.projectile.ProjectileMixin;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownExperienceBottle;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.bukkit.craftbukkit.event.CraftEventFactory;

@MixinInfo(events = {"ExpBottleEvent"})
@Mixin(ThrownExperienceBottle.class)
public class ThrownExperienceBottleMixin extends ProjectileMixin {

    @Inject(at = @At("HEAD"), method = "onHit", cancellable = true)
    public void doBukkitEvent(HitResult movingobjectposition, CallbackInfo ci) {
        HitResult.Type type = movingobjectposition.getType();
        if (type == HitResult.Type.ENTITY) {
            ((ThrownExperienceBottle)(Object)this).onHitEntity((EntityHitResult)movingobjectposition);
        } else if (type == HitResult.Type.BLOCK) this.onHitBlock((BlockHitResult)movingobjectposition);

        int i = 3 + this.mc_world().random.nextInt(5) + this.mc_world().random.nextInt(5);
        org.bukkit.event.entity.ExpBottleEvent event = CraftEventFactory.callExpBottleEvent((ThrownExperienceBottle)(Object)this, i);
        i = event.getExperience();

        while (i > 0) {
            int j = ExperienceOrb.getExperienceValue(i);
            i -= j;
            this.mc_world().addFreshEntity(new ExperienceOrb(this.mc_world(), ((Entity)(Object)this).getX(), ((Entity)(Object)this).getY(), ((Entity)(Object)this).getZ(), j));
        }
        this.removeBF();
        ci.cancel();
        return;
    }

}
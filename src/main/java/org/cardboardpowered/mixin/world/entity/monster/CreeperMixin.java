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
package org.cardboardpowered.mixin.world.entity.monster;

import org.cardboardpowered.bridge.world.entity.monster.CreeperBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.event.CraftEventFactory;

@Mixin(Creeper.class)
public abstract class CreeperMixin extends Entity implements CreeperBridge {

    public CreeperMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Final
    @Shadow
    private static EntityDataAccessor<Boolean> DATA_IS_POWERED;

    @Inject(at = @At("HEAD"), method="thunderHit", cancellable = true)
    public void invokeCreeperPowerEvent(ServerLevel worldserver, LightningBolt lightning, CallbackInfo ci) {
        super.thunderHit(worldserver, lightning);
        if (CraftEventFactory.callCreeperPowerEvent((Creeper)(Object)this, lightning, org.bukkit.event.entity.CreeperPowerEvent.PowerCause.LIGHTNING).isCancelled()) {
            ci.cancel();
            return;
        }
        this.cardboard$setPowered(true);
        ci.cancel();
        return;
    }

    @Override
    public void cardboard$setPowered(boolean powered) {
        this.entityData.set(DATA_IS_POWERED, powered);
    }
}
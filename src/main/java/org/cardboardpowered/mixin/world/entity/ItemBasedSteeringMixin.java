/**
 * Cardboard - Bukkit for Fabric
 * Copyright (C) 2023-2025 Cardboard contributors
 */
package org.cardboardpowered.mixin.world.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.ItemBasedSteering;
import org.cardboardpowered.bridge.world.entity.ItemBasedSteeringBridge;

@Mixin(ItemBasedSteering.class)
public class ItemBasedSteeringMixin implements ItemBasedSteeringBridge {

    @Shadow public SynchedEntityData entityData;
    @Shadow public EntityDataAccessor<Integer> boostTimeAccessor;
    @Shadow public boolean boosting;
    @Shadow public int boostTime; // field_23216
    // @Shadow public int currentBoostTime;

    @Override
    public void setBoostTicks(int ticks) {
        this.boosting = true;
        this.boostTime = 0;
        // this.currentBoostTime = ticks;
        this.entityData.set(this.boostTimeAccessor, ticks);
    }

}

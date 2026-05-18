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
package org.cardboardpowered.mixin.world.level.block.entity;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.cardboardpowered.bridge.world.level.block.entity.BeaconBlockEntityBridge;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin implements BeaconBlockEntityBridge {
    @Shadow
    @Nullable
    public Holder<MobEffect> primaryPower;

    @Shadow
    public int levels;

    @Shadow
    @Nullable
    public Holder<MobEffect> secondaryPower;

    // CraftBukkit start - add fields and methods
    @Override
    public org.bukkit.potion.@Nullable PotionEffect cardboard$getPrimaryEffect() {
        return (this.primaryPower != null)
                ? org.bukkit.craftbukkit.potion.CraftPotionUtil.toBukkit(new MobEffectInstance(
                this.primaryPower,
                BeaconBlockEntityBridge.computeEffectDuration(this.levels),
                BeaconBlockEntityBridge.computeEffectAmplifier(this.levels, this.primaryPower, this.secondaryPower),
                true,
                true
        ))
                : null;
    }

    @Override
    public org.bukkit.potion.@Nullable PotionEffect cardboard$getSecondaryEffect() {
        return (BeaconBlockEntityBridge.hasSecondaryEffect(this.levels, this.primaryPower, this.secondaryPower))
                ? org.bukkit.craftbukkit.potion.CraftPotionUtil.toBukkit(new MobEffectInstance(
                this.secondaryPower,
                BeaconBlockEntityBridge.computeEffectDuration(this.levels),
                BeaconBlockEntityBridge.computeEffectAmplifier(this.levels, this.primaryPower, this.secondaryPower),
                true,
                true
        ))
                : null;
    }
    // CraftBukkit end
    // Paper start - Custom beacon ranges
    @Unique
    private final String PAPER_RANGE_TAG = "Paper.Range";
    @Unique
    private double effectRange = -1;

    @Override
    public double cardboard$getEffectRange() {
        if (this.effectRange < 0) {
            return this.levels * 10 + 10;
        } else {
            return effectRange;
        }
    }

    @Override
    public void cardboard$setEffectRange(double range) {
        this.effectRange = range;
    }

    @Override
    public void cardboard$resetEffectRange() {
        this.effectRange = -1;
    }
    // Paper end - Custom beacon ranges
}

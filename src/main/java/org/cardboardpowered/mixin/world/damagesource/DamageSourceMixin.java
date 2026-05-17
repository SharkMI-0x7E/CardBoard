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
package org.cardboardpowered.mixin.world.damagesource;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.world.damagesource.DamageSource;
import org.cardboardpowered.bridge.world.damagesource.DamageSourceBridge;

@Mixin(DamageSource.class)
public class DamageSourceMixin implements DamageSourceBridge {

    private boolean sweep_BF;

    @Override
    public boolean isSweep_BF() {
        return sweep_BF;
    }

    @Override
    public DamageSource sweep_BF() {
        sweep_BF = true;
        return (DamageSource)(Object)this;
    }

}
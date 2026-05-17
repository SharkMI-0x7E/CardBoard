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
package org.cardboardpowered.mixin.world.entity.raid;

import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import org.cardboardpowered.bridge.world.entity.raid.RaidBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.Set;

@Mixin(Raid.class)
public class RaidMixin implements RaidBridge {
    @Shadow
    private Raid.RaidStatus status;

    @Shadow
    @Final
    private Map<Integer, Set<Raider>> groupRaiderMap;

    // CraftBukkit start
    @Override
    public boolean isInProgress() {
        return this.status == Raid.RaidStatus.ONGOING;
    }
    // CraftBukkit end

    // CraftBukkit start - a method to get all raiders
    @Override
    public java.util.Collection<Raider> getRaiders() {
        return this.groupRaiderMap.values().stream().flatMap(Set::stream).collect(java.util.stream.Collectors.toSet());
    }
    // CraftBukkit end
}
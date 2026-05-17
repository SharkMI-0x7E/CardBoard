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
package org.cardboardpowered.mixin.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Mixin;

import org.cardboardpowered.bridge.world.level.block.entity.BaseContainerBlockEntityBridge;
import org.cardboardpowered.bridge.world.level.LevelBridge;

@Mixin(BaseContainerBlockEntity.class)
public class BaseContainerBlockEntityMixin implements BaseContainerBlockEntityBridge {

    @Override
    public Location getLocation() {
        BaseContainerBlockEntity lc = (BaseContainerBlockEntity)(Object)this;
        BlockPos pos = lc.getBlockPos();
        return new Location(((LevelBridge)lc.level).cardboard$getWorld(), pos.getX(), pos.getY(), pos.getZ());
    }

}
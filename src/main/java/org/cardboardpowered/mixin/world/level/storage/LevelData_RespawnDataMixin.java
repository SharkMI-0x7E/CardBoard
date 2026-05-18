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
package org.cardboardpowered.mixin.world.level.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import org.cardboardpowered.bridge.world.level.storage.LevelData_RespawnDataBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelData.RespawnData.class)
public abstract class LevelData_RespawnDataMixin implements LevelData_RespawnDataBridge {
    @Shadow
    public abstract BlockPos pos();

    @Shadow
    @Final
    private float yaw;

    @Shadow
    @Final
    private float pitch;

    // Paper start
    @Override
    public LevelData.RespawnData cardboard$withLevel(ResourceKey<Level> dimension) {
        return new LevelData.RespawnData(GlobalPos.of(dimension, this.pos()), this.yaw, this.pitch);
    }

    /**
     * Equals without checking dimension.
     *
     * @param other other object
     * @return true if position and rotation are equal
     */
    @Override
    public boolean cardboard$positionEquals(Object other) {
        if (other == this) return true;
        if (!(other instanceof LevelData.RespawnData otherRespawn)) return false;
        return this.pos().equals(otherRespawn.pos())
                && this.yaw == otherRespawn.yaw()
                && this.pitch == otherRespawn.pitch();
    }
    // Paper end
}

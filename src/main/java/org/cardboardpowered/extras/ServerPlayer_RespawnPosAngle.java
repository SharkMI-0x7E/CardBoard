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
package org.cardboardpowered.extras;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

// CraftBukkit start
public record ServerPlayer_RespawnPosAngle(Vec3 position, float yaw, float pitch, boolean isBedSpawn, boolean isAnchorSpawn, @Nullable Runnable consumeAnchorCharge) {
    public static ServerPlayer_RespawnPosAngle of(Vec3 position, BlockPos towardsPos, float pitch, boolean isBedSpawn, boolean isAnchorSpawn, @Nullable Runnable consumeAnchorCharge) {
        return new ServerPlayer_RespawnPosAngle(position, calculateLookAtYaw(position, towardsPos), pitch, isBedSpawn, isAnchorSpawn, consumeAnchorCharge);
        // CraftBukkit end
    }

    private static float calculateLookAtYaw(Vec3 position, BlockPos towardsPos) {
        Vec3 vec3 = Vec3.atBottomCenterOf(towardsPos).subtract(position).normalize();
        return (float) Mth.wrapDegrees(Mth.atan2(vec3.z, vec3.x) * 180.0F / (float)Math.PI - 90.0);
    }
}
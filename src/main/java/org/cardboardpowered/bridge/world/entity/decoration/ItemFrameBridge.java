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
package org.cardboardpowered.bridge.world.entity.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public interface ItemFrameBridge {
    public static AABB createBoundingBoxStatic(BlockPos pos, Direction direction, boolean hasFramedMap) {
        float f = 0.46875F;
        Vec3 vec3 = Vec3.atCenterOf(pos).relative(direction, -0.46875);
        float f1 = hasFramedMap ? 1.0F : 0.75F;
        float f2 = hasFramedMap ? 1.0F : 0.75F;
        Direction.Axis axis = direction.getAxis();
        double d = axis == Direction.Axis.X ? 0.0625 : f1;
        double d1 = axis == Direction.Axis.Y ? 0.0625 : f2;
        double d2 = axis == Direction.Axis.Z ? 0.0625 : f1;
        return AABB.ofSize(vec3, d, d1, d2);
    }
}

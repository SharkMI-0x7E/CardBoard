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
package org.cardboardpowered.bridge.world.entity.decoration.painting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public interface PaintingBridge {
    public static AABB calculateBoundingBoxStatic(BlockPos pos, Direction direction, int width, int height) {
        float f = 0.46875F;
        Vec3 vec3 = Vec3.atCenterOf(pos).relative(direction, -0.46875);
        // CraftBukkit start
        double d = offsetForPaintingSize(width);
        double d1 = offsetForPaintingSize(height);
        // CraftBukkit end
        Direction counterClockWise = direction.getCounterClockWise();
        Vec3 vec31 = vec3.relative(counterClockWise, d).relative(Direction.UP, d1);
        Direction.Axis axis = direction.getAxis();
        // CraftBukkit start
        double d2 = axis == Direction.Axis.X ? 0.0625 : width;
        double d3 = height;
        double d4 = axis == Direction.Axis.Z ? 0.0625 : width;
        // CraftBukkit end
        return AABB.ofSize(vec31, d2, d3, d4);
    }

    private static double offsetForPaintingSize(int size) { // CraftBukkit - static
        return size % 2 == 0 ? 0.5 : 0.0;
    }
}

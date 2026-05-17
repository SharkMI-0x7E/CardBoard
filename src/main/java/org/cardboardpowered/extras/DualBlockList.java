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
package org.cardboardpowered.extras;

import org.bukkit.World;
import org.bukkit.block.Block;
import java.util.AbstractList;
import java.util.List;
import net.minecraft.core.BlockPos;

public class DualBlockList extends AbstractList<Block> {
    private final List<BlockPos> moved;
    private final List<BlockPos> broken;
    private final World world;

    public DualBlockList(List<BlockPos> moved, List<BlockPos> broken, World world) {
        this.moved = moved;
        this.broken = broken;
        this.world = world;
    }

    @Override
    public org.bukkit.block.Block get(int index) {
        if (index >= this.size() || index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        BlockPos pos = index < this.moved.size() ? this.moved.get(index) : this.broken.get(index - this.moved.size());
        return this.world.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public int size() {
        return this.moved.size() + this.broken.size();
    }
}

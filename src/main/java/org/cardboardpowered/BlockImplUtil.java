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
package org.cardboardpowered;

import org.bukkit.craftbukkit.block.data.IMagicNumbers;
import net.minecraft.core.Direction;
import org.bukkit.block.BlockFace;

public class BlockImplUtil {

    public static BlockFace notchToBlockFace(Direction notch) {
        if (notch == null) return BlockFace.SELF;
        return BlockFace.valueOf(notch.name());
    }

    public static Direction blockFaceToNotch(BlockFace face) {
        return Direction.valueOf(face.name());
    }
    
    public static IMagicNumbers MN = null;
    public static void setMN(IMagicNumbers mn) {
        MN = mn;
    }


}
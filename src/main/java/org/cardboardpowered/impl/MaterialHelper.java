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
package org.cardboardpowered.impl;

import org.bukkit.Material;

@Deprecated(forRemoval = true)
public class MaterialHelper {

    public static boolean isBlock(Material mat) {
       return false;
    }

    public static boolean isEdible(Material mat) {
        return false;
    }

    public static float getHardness(Material material) {
        return 0;
    }

    public static float getBlastResistance(Material material) {
        return 0;
    }

}
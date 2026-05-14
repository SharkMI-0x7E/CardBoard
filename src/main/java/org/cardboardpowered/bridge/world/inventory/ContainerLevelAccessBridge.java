/**
 * The Bukkit for Fabric Project
 * Copyright (C) 2020-2025 CardboardPowered.org and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 */
package org.cardboardpowered.bridge.world.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 */
public interface ContainerLevelAccessBridge {

    org.bukkit.Location getLocation();

    Level getWorld();

    BlockPos getPosition();

}
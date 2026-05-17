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

import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

// TODO: Mixin TeleportTarget
public class TeleportTargetExtra {

	public static TeleportTransition newTeleportTarget(ServerLevel level, Entity entity, TeleportTransition.PostTeleportTransition trans) {
        return new TeleportTransition(
           level,
           getWorldSpawnPos(level, entity),
           Vec3.ZERO,
           level.getRespawnData().yaw(),
           level.getRespawnData().pitch(),
           false,
           false,
           Set.of(),
           trans
           // TeleportCause.UNKNOWN
        );
     }
    
    private static Vec3 getWorldSpawnPos(ServerLevel world, Entity entity) {
        return entity.adjustSpawnLocation(world, world.getRespawnData().pos()).getBottomCenter();
     }
	
}

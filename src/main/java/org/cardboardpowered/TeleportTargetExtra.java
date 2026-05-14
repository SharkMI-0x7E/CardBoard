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

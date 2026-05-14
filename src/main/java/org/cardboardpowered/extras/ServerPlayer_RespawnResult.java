package org.cardboardpowered.extras;

import net.minecraft.world.level.portal.TeleportTransition;

public record ServerPlayer_RespawnResult(TeleportTransition transition, boolean isBedSpawn, boolean isAnchorSpawn) {
}
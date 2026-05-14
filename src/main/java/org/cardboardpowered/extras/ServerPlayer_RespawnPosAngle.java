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
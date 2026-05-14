package org.cardboardpowered.mixin.world.level.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import org.cardboardpowered.bridge.world.level.storage.LevelData_RespawnDataBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelData.RespawnData.class)
public abstract class LevelData_RespawnDataMixin implements LevelData_RespawnDataBridge {
    @Shadow
    public abstract BlockPos pos();

    @Shadow
    @Final
    private float yaw;

    @Shadow
    @Final
    private float pitch;

    // Paper start
    @Override
    public LevelData.RespawnData cardboard$withLevel(ResourceKey<Level> dimension) {
        return new LevelData.RespawnData(GlobalPos.of(dimension, this.pos()), this.yaw, this.pitch);
    }

    /**
     * Equals without checking dimension.
     *
     * @param other other object
     * @return true if position and rotation are equal
     */
    @Override
    public boolean cardboard$positionEquals(Object other) {
        if (other == this) return true;
        if (!(other instanceof LevelData.RespawnData otherRespawn)) return false;
        return this.pos().equals(otherRespawn.pos())
                && this.yaw == otherRespawn.yaw()
                && this.pitch == otherRespawn.pitch();
    }
    // Paper end
}

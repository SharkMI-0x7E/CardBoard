package org.cardboardpowered.bridge.world.level.storage;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;

public interface LevelData_RespawnDataBridge {
    LevelData.RespawnData cardboard$withLevel(ResourceKey<Level> dimension);

    boolean cardboard$positionEquals(Object other);
}

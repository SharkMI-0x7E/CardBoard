/**
 * Cardboard - Bukkit for Fabric Project.
 * Copyright (C) 2020 Cardboard contributors
 */
package org.cardboardpowered.bridge.world.level.storage;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface PrimaryLevelDataBridge {

    void checkName(String name);

    ResourceKey<Level> cardboard$getRespawnDimension();

    void cardboard$setRespawnDimension(ResourceKey<Level> respawnDimension);
}
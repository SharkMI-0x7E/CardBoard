package org.cardboardpowered.bridge.world.level.storage;

import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.validation.ContentValidationException;

public interface LevelStorageSourceBridge {

	Path getStorageFolder(Path path, ResourceKey<LevelStem> dimensionType);

	LevelStorageAccess validateAndCreateAccess(String saveName, ResourceKey<LevelStem> dimensionType)
			throws IOException, ContentValidationException;

}

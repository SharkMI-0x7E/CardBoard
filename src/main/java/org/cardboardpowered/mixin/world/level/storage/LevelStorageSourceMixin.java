package org.cardboardpowered.mixin.world.level.storage;

import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.validation.ContentValidationException;
import org.cardboardpowered.bridge.world.level.storage.LevelStorageSourceBridge;
import org.cardboardpowered.bridge.world.level.storage.LevelStorageSource_LevelStorageAccessBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelStorageSource.class)
public class LevelStorageSourceMixin implements LevelStorageSourceBridge {

	@Override
	public Path getStorageFolder(Path path, ResourceKey<LevelStem> dimensionType) {
		if (dimensionType == LevelStem.OVERWORLD) {
			return path;
		} else if (dimensionType == LevelStem.NETHER) {
			return path.resolve("DIM-1");
		} else {
			return dimensionType == LevelStem.END
					? path.resolve("DIM1")
							: path.resolve("dimensions").resolve(dimensionType.identifier().getNamespace()).resolve(dimensionType.identifier().getPath());
		}
	}
	
	@Override
	public LevelStorageSource.LevelStorageAccess validateAndCreateAccess(String saveName, ResourceKey<LevelStem> dimensionType) throws IOException, ContentValidationException {
		LevelStorageSource.LevelStorageAccess vanilla = this.validateAndCreateAccess(saveName);
		((LevelStorageSource_LevelStorageAccessBridge) vanilla).cardboard$set_dimensionType(dimensionType); // Paper-ize
		return vanilla;
	}
	
	@Shadow
	public LevelStorageAccess validateAndCreateAccess( String directoryName) {
		return null; // Shadowed
	}
	
}

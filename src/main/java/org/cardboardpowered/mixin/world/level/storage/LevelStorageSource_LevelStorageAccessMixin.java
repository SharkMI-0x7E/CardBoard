package org.cardboardpowered.mixin.world.level.storage;

import java.nio.file.Path;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.cardboardpowered.bridge.world.level.storage.LevelStorageSource_LevelStorageAccessBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelStorageSource.LevelStorageAccess.class)
public class LevelStorageSource_LevelStorageAccessMixin implements LevelStorageSource_LevelStorageAccessBridge {
	
	@Shadow
	public LevelStorageSource.LevelDirectory levelDirectory;

	// Paper - Add dimensionType
	public ResourceKey<LevelStem> dimensionType;
	
	@Override
	public void cardboard$set_dimensionType(ResourceKey<LevelStem> value) {
		this.dimensionType = value;
	}
	
	@Override
	public ResourceKey<LevelStem> cardboard$get_dimensionType() {
		return this.dimensionType;
	}

	@Overwrite
	public Path getDimensionPath(ResourceKey<Level> key) {
		if (null == this.dimensionType) {
			// Non-Bukkit
			return DimensionType.getStorageFolder(key, this.levelDirectory.path());
		}
		
		return LevelStorage_getStorageFolder(this.levelDirectory.path(), this.dimensionType);
	}

	private Path LevelStorage_getStorageFolder(Path path, ResourceKey<LevelStem> dimensionType) {
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
	
}

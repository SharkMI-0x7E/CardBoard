package org.cardboardpowered.mixin.world.level.storage;

import java.nio.file.Path;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.cardboardpowered.bridge.world.level.storage.LevelStorageSource_LevelStorageAccessBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(LevelStorageSource.LevelStorageAccess.class)
public class LevelStorageSource_LevelStorageAccessMixin implements LevelStorageSource_LevelStorageAccessBridge {
	
	@Shadow
	public LevelStorageSource.LevelDirectory levelDirectory;

	public ResourceKey<LevelStem> dimensionType;
	
	@Override
	public void cardboard$set_dimensionType(ResourceKey<LevelStem> value) {
		this.dimensionType = value;
	}
	
	@Override
	public ResourceKey<LevelStem> cardboard$get_dimensionType() {
		return this.dimensionType;
	}

	@Inject(at = @At("RETURN"), method = "getDimensionPath", cancellable = true)
	public void cardboard$onGetDimensionPath(ResourceKey<Level> key, CallbackInfoReturnable<Path> cir) {
		if (this.dimensionType == null) {
			return;
		}
		cir.setReturnValue(LevelStorage_getStorageFolder(this.levelDirectory.path(), this.dimensionType));
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

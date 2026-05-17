/**
 * Copyright (C) 2026 SharkMI and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 */
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

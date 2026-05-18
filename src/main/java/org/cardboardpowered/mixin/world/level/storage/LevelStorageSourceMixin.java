/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2024 CardboardPowered.org and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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

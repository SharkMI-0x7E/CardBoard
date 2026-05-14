/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2022
 */
package org.cardboardpowered.bridge.world.level.chunk;

import org.bukkit.Chunk;

public interface LevelChunkBridge {

    //Map<Heightmap.Type, Heightmap> getHeightMaps();

    //TypeFilterableList<Entity>[] getEntitySections();

    Chunk getBukkitChunk();

    /*
	public default BlockState setBlockState(BlockPos blockposition, BlockState iblockdata, boolean moved, boolean doPlace) {
		return null;
	}
	*/

}
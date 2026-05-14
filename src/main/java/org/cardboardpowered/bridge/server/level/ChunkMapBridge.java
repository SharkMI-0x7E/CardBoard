package org.cardboardpowered.bridge.server.level;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.level.ChunkHolder;

public interface ChunkMapBridge {

    Long2ObjectLinkedOpenHashMap<ChunkHolder> getChunkHoldersBF();

}
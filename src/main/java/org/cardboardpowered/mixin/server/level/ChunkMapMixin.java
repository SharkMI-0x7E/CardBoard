package org.cardboardpowered.mixin.server.level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import org.cardboardpowered.bridge.server.level.ChunkMapBridge;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;

@Mixin(ChunkMap.class)
public class ChunkMapMixin implements ChunkMapBridge {

    @Shadow
    public Long2ObjectLinkedOpenHashMap<ChunkHolder> visibleChunkMap;

    @Override
    public Long2ObjectLinkedOpenHashMap<ChunkHolder> getChunkHoldersBF() {
        return visibleChunkMap;
    }

}

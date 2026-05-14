package org.cardboardpowered.bridge.server.level;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public interface ChunkHolderBridge {

    // CraftBukkit start
    /*static WorldChunk getFullChunk(ChunkHolder holder) {
        if (!ChunkHolder.getLevelType(holder.lastTickLevel).isAfter(ChunkHolder.LevelType.BORDER)) return null; // note: using oldTicketLevel for isLoaded checks
        CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> statusFuture = holder.getFutureFor(ChunkStatus.FULL);
        Either<Chunk, ChunkHolder.Unloaded> either = statusFuture.getNow(null);
        return either == null ? null : (WorldChunk) either.left().orElse(null);
    }*/
    // CraftBukkit end

    
    static LevelChunk getFullChunkNow(ChunkHolder holder) {
    	 if (!ChunkLevel.fullStatus(holder.oldTicketLevel).isOrAfter(FullChunkStatus.FULL)) {
             return null; // note: using oldTicketLevel for isLoaded checks
         }
         return getFullChunkNowUnchecked(holder);
    }

    static LevelChunk getFullChunkNowUnchecked(ChunkHolder holder) {
    	// CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> statusFuture = holder.getFutureFor(ChunkStatus.FULL);
        // Either<Chunk, ChunkHolder.Unloaded> either = statusFuture.getNow(null);
        // return (either == null) ? null : (WorldChunk) either.left().orElse(null);
        
    	return (LevelChunk) holder.getChunkIfPresentUnchecked(ChunkStatus.FULL);
    	
    	// CompletableFuture<OptionalChunk<Chunk>> statusFuture = holder.getFutureFor(ChunkStatus.FULL);
    	// OptionalChunk<Chunk>  either = statusFuture.getNow(null);
        // return (either == null) ? null : (WorldChunk) either.orElse(null);
        
    }

}
/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors
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
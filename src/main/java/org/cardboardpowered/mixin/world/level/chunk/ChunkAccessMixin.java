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
package org.cardboardpowered.mixin.world.level.chunk;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Maps;
import org.cardboardpowered.bridge.world.level.chunk.ChunkAccessBridge;

@Mixin(ChunkAccess.class)
public abstract class ChunkAccessMixin implements ChunkAccessBridge {

    public Registry<Biome> biomeRegistry;
	
    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$init(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, Registry<Biome>  registry, long l, LevelChunkSection[] levelChunkSections, BlendingData blendingData, CallbackInfo ci) {
        this.biomeRegistry = registry;
    }
    
	@Shadow
	public final Map<BlockPos, BlockEntity> blockEntities = Maps.newHashMap();

	@Override
	public Map<BlockPos, BlockEntity> cardboard_getBlockEntities() {
		return blockEntities;
	}
	
    @Override
    public Registry<Biome> bridge$biomeRegistry() {
        return biomeRegistry;
    }

}

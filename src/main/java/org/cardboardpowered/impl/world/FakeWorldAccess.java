/**
 * CardboardPowered - Bukkit/Spigot for Fabric
 * Copyright (C) CardboardPowered.org and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.impl.world;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import org.bukkit.craftbukkit.CraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.attribute.EnvironmentAttributeReader;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTickAccess;

public class FakeWorldAccess implements LevelAccessor {

    public static final LevelAccessor INSTANCE = new FakeWorldAccess();

    protected FakeWorldAccess() {
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return null;//TODO
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return null;//TODO
    }

    @Override
    public LevelData getLevelData() {
        throw new UnsupportedOperationException("Not supported");
    }

    // @Override
    public DifficultyInstance getLocalDifficulty(BlockPos blockposition) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public ChunkSource getChunkSource() {
        throw new UnsupportedOperationException("Not supported");
    }

    /*@Override
    public Random getRandom() {
        throw new UnsupportedOperationException("Not supported");
    }*/

	@Override
	public void addParticle(ParticleOptions parameters, double x, double y, double z, double velocityX,
			double velocityY, double velocityZ) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public void playSound(Entity arg0, BlockPos arg1, SoundEvent arg2, SoundSource arg3, float arg4, float arg5) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public void levelEvent(Entity arg0, int arg1, BlockPos arg2, int arg3) {
		throw new UnsupportedOperationException("Not supported");
	}

    @Override
    public RegistryAccess registryAccess() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public List<Entity> getEntities(Entity entity, AABB aabb, Predicate<? super Entity> prdct) {
        throw new UnsupportedOperationException("Not supported");
    }

   //@Override
   // public <T extends Entity> List<T> getEntitiesByClass(Class<? extends T> type, Box aabb, Predicate<? super T> prdct) {
   //     throw new UnsupportedOperationException("Not supported yet.");
   // }

    @Override
    public List<? extends Player> players() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public ChunkAccess getChunk(int i, int i1, ChunkStatus cs, boolean bln) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public int getHeight(Heightmap.Types type, int i, int i1) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public int getSkyDarken() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public BiomeManager getBiomeManager() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    // 1.18.1: Biome
    // 1.18.2: RegistryEntry<Biome> 
    public Holder<Biome> getUncachedNoiseBiome(int i, int i1, int i2) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean isClientSide() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public int getSeaLevel() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public DimensionType dimensionType() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public LevelLightEngine getLightEngine() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos blockposition) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public BlockState getBlockState(BlockPos blockposition) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public FluidState getFluidState(BlockPos blockposition) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public WorldBorder getWorldBorder() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean isStateAtPosition(BlockPos bp, Predicate<BlockState> prdct) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean setBlock(BlockPos blockposition, BlockState iblockdata, int i, int j) {
        return false;
    }

    @Override
    public boolean removeBlock(BlockPos blockposition, boolean flag) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean destroyBlock(BlockPos blockposition, boolean flag, Entity entity, int i) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public float getShade(Direction arg0, boolean arg1) {
        return 0;
    }

    @Override
    public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> filter, AABB box,
            Predicate<? super T> predicate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isFluidAtPosition(BlockPos pos, Predicate<FluidState> state) {
        // TODO Auto-generated method stub
        return false;
    }

    // @Override
    public void emitGameEvent(Entity arg0, GameEvent arg1, BlockPos arg2) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public MinecraftServer getServer() {
        // TODO Auto-generated method stub
        return CraftServer.server;
    }

    // TODO
    public long nextSubTickCount() {
        // TODO Auto-generated method stub
        return 0;
    }

	// @Override
	public void emitGameEvent(GameEvent event, Vec3 emitterPos, Context emitter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public net.minecraft.util.RandomSource getRandom() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FeatureFlagSet enabledFeatures() {
		// TODO Auto-generated method stub
		return null;
	}

	// @Override
	public void gameEvent(Holder<GameEvent> event, Vec3 emitterPos, Context emitter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public EnvironmentAttributeReader environmentAttributes() {
		return EnvironmentAttributeReader.EMPTY;
	}

}
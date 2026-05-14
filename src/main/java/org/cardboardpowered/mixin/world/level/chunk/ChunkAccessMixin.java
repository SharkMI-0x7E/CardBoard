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

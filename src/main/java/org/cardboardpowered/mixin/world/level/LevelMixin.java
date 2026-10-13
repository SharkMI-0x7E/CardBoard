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
package org.cardboardpowered.mixin.world.level;

import org.cardboardpowered.CardboardMod;
import org.cardboardpowered.bridge.world.level.LevelBridge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.storage.WritableLevelData;
import org.bukkit.craftbukkit.block.CapturedBlockState;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.cardboardpowered.impl.world.CraftWorld;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(Level.class)
public abstract class LevelMixin implements LevelBridge {

    @Shadow public LevelChunk getChunkAt(BlockPos pos) {return null;}
    private CraftWorld bukkit;

    public boolean captureBlockStates = false;
    public boolean captureTreeGeneration = false;
    // LinkedHashMap to retain block placement order during tree capture replay.
    public Map<BlockPos, CraftBlockState> capturedBlockStates = new LinkedHashMap<>();

    @Shadow
    public abstract LevelEntityGetter<Entity> getEntities();

    @Shadow
    public ResourceKey<Level> dimension() {
        return null;
    }

    @Override
    public LevelEntityGetter<Entity> cb$get_entity_lookup() {
    	return getEntities();
    }
    
    @Override
    public Map<BlockPos, CraftBlockState> getCapturedBlockStates_BF() {
        return capturedBlockStates;
    }

    @Override
    public boolean isCaptureBlockStates_BF() {
        return captureBlockStates;
    }
    
    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(WritableLevelData a, ResourceKey<?> b, RegistryAccess rm, Holder<DimensionType> registryEntry, boolean f, boolean g, long h, int i, CallbackInfo ci) {

        if (!(((Level) (Object) this) instanceof ServerLevel)) {
            System.out.println("CLIENT WORLD!");
            return;
        }

        Level thiz = (Level) (Object) this;
        ServerLevel nms = ((ServerLevel) thiz);
    	CardboardMod.on_world_init_mc(nms);
    }

    @Override
    public CraftWorld cardboard$getWorld() {
        return bukkit;
    }

    @Override
    public void set_bukkit_world(CraftWorld world) {
        this.bukkit = world;
    }

    // Paper-aligned block-state capture at Level#setBlock head (two branches):
    //
    // 1) captureTreeGeneration (CraftWorld#generateTree tree/delegate paths):
    //    blocks are NEVER written to the live world. Bedrock and end portal/frames
    //    are protected (Paper's isDestroyable() allowlist), the NEW BlockState is
    //    stored on a captured snapshot that tree features re-read via getBlockState
    //    below, and the vanilla method short-circuits with true. Callers replay the
    //    captured states themselves.
    //
    // 2) captureBlockStates alone (ItemStackMixin#useOn -> BlockPlaceEvent):
    //    a snapshot of the OLD state is recorded but the world IS written; the
    //    placement event is built from these snapshots after item.useOn returns and
    //    rolls back when cancelled. The block-entity capture branch from Paper
    //    (capturedTileEntities) is not ported because nothing here reads it.
    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("HEAD"), cancellable = true)
    public void cardboard$captureSetBlock(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
        if (this.captureTreeGeneration) {
            Level self = (Level) (Object) this;
            Block existing = self.getBlockState(pos).getBlock();
            if (existing == Blocks.BEDROCK
                    || existing == Blocks.END_PORTAL
                    || existing == Blocks.END_PORTAL_FRAME) {
                cir.setReturnValue(false);
                return;
            }
            CraftBlockState blockstate = this.capturedBlockStates.get(pos);
            if (blockstate == null) {
                blockstate = CapturedBlockState.getTreeBlockState(self, pos, flags);
                this.capturedBlockStates.put(pos.immutable(), blockstate);
            }
            blockstate.setData(state);
            blockstate.setFlags(flags);
            cir.setReturnValue(true);
        } else if (this.captureBlockStates
                && !this.capturedBlockStates.containsKey(pos)) {
            // OLD-state snapshot for BlockPlaceEvent; vanilla setBlock still runs.
            this.capturedBlockStates.put(pos.immutable(),
                    (CraftBlockState) CraftBlock.at((Level) (Object) this, pos).getState());
        }
    }

    // During tree capture, reads must see the already-proposed NEW states, otherwise
    // features place blocks against stale world contents.
    @Inject(method = "getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("HEAD"), cancellable = true)
    public void cardboard$captureTreeGenerationGetBlockState(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (!this.captureTreeGeneration) {
            return;
        }
        CraftBlockState previous = this.capturedBlockStates.get(pos);
        if (previous != null) {
            cir.setReturnValue(previous.getHandle());
        }
    }

    @Override
    public void setCaptureBlockStates_BF(boolean b) {
        this.captureBlockStates = b;
    }

    @Override
    public boolean isCaptureTreeGeneration_BF() {
        return this.captureTreeGeneration;
    }

    @Override
    public void setCaptureTreeGeneration_BF(boolean b) {
        this.captureTreeGeneration = b;
    }
}

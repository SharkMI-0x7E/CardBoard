/**
 * Copyright (C) 2026 SharkMI and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 */
package org.cardboardpowered.mixin.world.level.chunk;

import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.CraftChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.cardboardpowered.bridge.world.level.chunk.LevelChunkBridge;

@Mixin(LevelChunk.class)
public class LevelChunkMixin implements LevelChunkBridge {

    private Chunk bukkit;

    @Inject(method = "<init>*", at = @At("TAIL"))
    public void setBukkitChunk(CallbackInfo ci) {
        try {
            cardboard_set();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Chunk getBukkitChunk() {
        cardboard_set();
        return bukkit;
    }
    
    public void cardboard_set() {
        if (null == bukkit) {
            this.bukkit = new CraftChunk((LevelChunk)(Object)this);
        }
    }

    /*
    @Override
    public BlockState setBlockState(BlockPos blockposition, BlockState iblockdata, boolean moved, boolean doPlace) {
    	// TODO: support doPlace
    	return ((WorldChunk)(Object)this).setBlockState(blockposition, iblockdata, moved);
    }
    */

}

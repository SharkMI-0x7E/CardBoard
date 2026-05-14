package org.cardboardpowered.mixin.server.level;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.server.level.ChunkHolder;
import org.cardboardpowered.bridge.server.level.ChunkHolderBridge;

@Mixin(ChunkHolder.class)
public class ChunkHolderMixin implements ChunkHolderBridge {
}
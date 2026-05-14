package org.cardboardpowered.bridge.world.level.block.entity;

import net.minecraft.core.BlockPos;

import java.util.List;

public interface ConduitBlockEntityBridge {
    public static int getRange(List<BlockPos> positions) {
        int size = positions.size();
        int i = size / 7 * 16;
        return i;
    }
}

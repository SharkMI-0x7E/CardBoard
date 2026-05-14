package org.cardboardpowered.bridge.world.item.component;

import net.minecraft.nbt.CompoundTag;

public interface CustomDataBridge {
    @Deprecated
    CompoundTag cardboard$getUnsafe();

    boolean cardboard$contains(String key);
}

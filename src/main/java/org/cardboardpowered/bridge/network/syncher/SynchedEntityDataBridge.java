package org.cardboardpowered.bridge.network.syncher;

import net.minecraft.network.syncher.EntityDataAccessor;

public interface SynchedEntityDataBridge {

    <T> void markDirty(EntityDataAccessor<T> key);
}

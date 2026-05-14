package org.cardboardpowered.mixin.network.syncher;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import org.cardboardpowered.bridge.network.syncher.SynchedEntityDataBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SynchedEntityData.class)
public abstract class SynchedEntityDataMixin implements SynchedEntityDataBridge {

    @Shadow protected abstract <T> SynchedEntityData.DataItem<T> getItem(EntityDataAccessor<T> trackedData);

    @Shadow private boolean isDirty;

    @Override
    public <T> void markDirty(EntityDataAccessor<T> key) {
        SynchedEntityData.DataItem entry = this.getItem(key);
        entry.setDirty(true);
        this.isDirty = true;
    }
}

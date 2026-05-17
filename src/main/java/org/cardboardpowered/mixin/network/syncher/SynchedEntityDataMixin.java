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

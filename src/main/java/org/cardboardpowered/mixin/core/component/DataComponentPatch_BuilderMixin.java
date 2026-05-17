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
package org.cardboardpowered.mixin.core.component;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;

import org.cardboardpowered.bridge.core.component.DataComponentPatch_BuilderBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;

@Mixin(DataComponentPatch.Builder.class)
public class DataComponentPatch_BuilderMixin implements DataComponentPatch_BuilderBridge {
    @Shadow @Final
    public Reference2ObjectMap<DataComponentType<?>, Optional<?>> map;

    // CraftBukkit start
    @Override
    public void copy(DataComponentPatch orig) {
        this.map.putAll(orig.map);
    }

    @Override
    public void clear(DataComponentType<?> type) {
        this.map.remove(type);
    }

    public boolean isSet(DataComponentType<?> type) {
        return this.map.containsKey(type);
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object instanceof DataComponentPatch.Builder patch) {
            return this.map.equals(patch.map);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.map.hashCode();
    }
    // CraftBukkit end
}

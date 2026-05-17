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
package org.cardboardpowered.mixin.core;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;

@Mixin(RegistryAccess.Frozen.class)
public interface RegistryAccess_FrozenMixin {

    /**
     * Bridge method required by WorldEdit:
     * IdMap lookupOrThrow(ResourceKey)
     */
    @SuppressWarnings("unchecked")
    public default /*IdMap<?>*/ Registry<?> cardboard$lookupOrThrow(ResourceKey<?> key) {
        // Call the real method (returns Registry)
        Registry<?> reg = ((RegistryAccess.Frozen)(Object)(this)).lookupOrThrow((ResourceKey<? extends Registry<?>>) key);

        // Registry implements IdMap, so this cast is valid
        return reg; // (IdMap<?>) reg;
    }
}

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
package org.cardboardpowered.bridge.world.level.storage;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;

public interface TagValueOutputBridge {
    // Paper start - utility methods
    public static TagValueOutput createWrappingGlobal(
            final ProblemReporter problemReporter,
            final CompoundTag output
    ) {
        return new TagValueOutput(problemReporter, NbtOps.INSTANCE, output);
    }

    public static TagValueOutput createWrappingWithContext(
            final ProblemReporter problemReporter,
            final HolderLookup.Provider lookup,
            final CompoundTag output
    ) {
        return new TagValueOutput(problemReporter, lookup.createSerializationContext(NbtOps.INSTANCE), output);
    }
    // Paper end - utility methods
}

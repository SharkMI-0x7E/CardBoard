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

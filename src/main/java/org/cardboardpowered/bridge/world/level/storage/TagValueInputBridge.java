package org.cardboardpowered.bridge.world.level.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.bukkit.craftbukkit.CraftServer;

public interface TagValueInputBridge {
    // Paper start - utility methods
    public static ValueInput createGlobal(
            final ProblemReporter problemReporter,
            final CompoundTag compoundTag
    ) {
        return TagValueInput.create(problemReporter, CraftServer.INSTANCE.getServer().registryAccess(), compoundTag);
    }
    // Paper end - utility methods
}

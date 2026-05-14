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

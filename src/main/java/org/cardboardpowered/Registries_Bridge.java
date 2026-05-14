package org.cardboardpowered;

import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import com.mojang.serialization.Lifecycle;

import io.papermc.paper.registry.data.util.Conversions;

public class Registries_Bridge {

    public static final Conversions BUILT_IN_CONVERSIONS = new Conversions(new RegistryOps.RegistryInfoLookup(){

        public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> registryRef) {
            HolderLookup.RegistryLookup registry = RegistryLayer.STATIC_ACCESS.lookupOrThrow((ResourceKey)registryRef);
            return Optional.of(new RegistryOps.RegistryInfo<T>(registry, registry, Lifecycle.experimental()));
        }
    });
	
}

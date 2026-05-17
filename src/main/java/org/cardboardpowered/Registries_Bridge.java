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

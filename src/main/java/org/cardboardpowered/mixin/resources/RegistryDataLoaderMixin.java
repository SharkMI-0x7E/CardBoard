/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2024 CardboardPowered.org and contributors
 * Copyright (C) 2025-2026 SharkMI and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.mixin.resources;

import java.util.*;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.serialization.Decoder;

import io.papermc.paper.registry.PaperRegistryAccess;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
//import net.minecraft.registry.RegistryLoader.Loader;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryDataLoader.Loader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;

@Mixin(RegistryDataLoader.class)
public class RegistryDataLoaderMixin {

    @Inject(
    		at = @At(value = "RETURN"),
    		method = "loadContentsFromManager(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/RegistryOps$RegistryInfoLookup;Lnet/minecraft/core/WritableRegistry;Lcom/mojang/serialization/Decoder;Ljava/util/Map;)V",
    		locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void cardboard$reg_lock_reference_holders (
    		ResourceManager resourceManager,
    		RegistryOps.RegistryInfoLookup infoGetter,
    		WritableRegistry registry,
    		Decoder elementDecoder, Map<ResourceKey<?>, Exception> errors,
    		CallbackInfo ci) {

    	 PaperRegistryAccess.instance().lockReferenceHolders(registry.key());
         // PaperRegistryListenerManager.INSTANCE.runFreezeListeners(registry.getKey(), conversions);
    }
    
    /**
     * @author Cardboard
     * @reason Paper: add method to get the value for pre-filling builders in the reg mod API
     *
     * TODO: Cannot replace with @Inject - this @Overwrite completely replaces the
     * createContext method to add Paper's lookupForValueCopyViaBuilders() method
     * which provides HolderLookup.Provider for registry builders. The original
     * method is extended with Paper-specific registry context functionality.
     */
    @Overwrite
    private static RegistryOps.RegistryInfoLookup createContext(List<HolderLookup.RegistryLookup<?>> registries, List<Loader<?>> additionalRegistries) {
        final HashMap<ResourceKey<? extends Registry<?>>, RegistryOps.RegistryInfo<?>> map = new HashMap<>();
        registries.forEach(registry -> map.put(registry.key(), createInfoForContextRegistry(registry)));
        additionalRegistries.forEach(loader -> map.put(loader.registry().key(), createInfoForNewRegistry(loader.registry())));
        
        // Cardboard: Paper: providerForBuilders
        HolderLookup.Provider providerForBuilders = HolderLookup.Provider.create(Stream.concat(registries.stream(), additionalRegistries.stream().map(Loader::registry)));
        
        return new RegistryOps.RegistryInfoLookup(){

            @Override
            public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> registryRef) {
                return Optional.ofNullable((RegistryOps.RegistryInfo<T>)map.get(registryRef));
            }
            
            // @Override
            public HolderLookup.Provider lookupForValueCopyViaBuilders() {
                return providerForBuilders;
            }
            
        };
    }
    
    @Shadow
    private static <T> RegistryOps.RegistryInfo<T> createInfoForNewRegistry(WritableRegistry<T> registry) {
        return new RegistryOps.RegistryInfo<T>(registry, registry.createRegistrationLookup(), registry.registryLifecycle());
    }

    @Shadow
    private static <T> RegistryOps.RegistryInfo<T> createInfoForContextRegistry(HolderLookup.RegistryLookup<T> registry) {
        return new RegistryOps.RegistryInfo<T>(registry, registry, registry.registryLifecycle());
    }
    
}

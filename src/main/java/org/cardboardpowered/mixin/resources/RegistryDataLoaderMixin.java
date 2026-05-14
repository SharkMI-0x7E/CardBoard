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

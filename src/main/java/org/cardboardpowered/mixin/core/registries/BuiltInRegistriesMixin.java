package org.cardboardpowered.mixin.core.registries;

import java.util.Map;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.cardboardpowered.CardboardMod;

import io.papermc.paper.registry.PaperRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.BuiltInRegistries.RegistryBootstrap;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;

@Mixin(BuiltInRegistries.class)
public class BuiltInRegistriesMixin {
	
	@Shadow
    private static Map<Identifier, Supplier<?>> LOADERS;

	@Shadow
    private static WritableRegistry<WritableRegistry<?>> WRITABLE_REGISTRY;
	
	@Inject(at = @At("HEAD"), method = "internalRegister(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/WritableRegistry;Lnet/minecraft/core/registries/BuiltInRegistries$RegistryBootstrap;)Lnet/minecraft/core/WritableRegistry;")
	private static void testtt(ResourceKey key, WritableRegistry registry, RegistryBootstrap initializer, CallbackInfoReturnable ci) {
		
		Bootstrap.checkBootstrapCalled(() -> "registry " + key.identifier());
		PaperRegistryAccess.instance().registerRegistry(registry.key(), registry);
	}
    
    //@Shadow
    //private static void init() {}
    
    /*
    @Inject(at = @At("HEAD"), method = "init")
    private static void init_bukkit() {
    	try {
			Class.forName(org.bukkit.Registry.class.getName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
    }
    */
    
	/**
	 * @author Cardboard Mod
	 * @reason PaperRegistryAccess
	 */
    @Overwrite
    public static void createContents() {
    	try {
			Class.forName(org.bukkit.Registry.class.getName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}

    	LOADERS.forEach((id, initializer) -> {
            if (initializer.get() == null) {
                CardboardMod.LOGGER.warning("Unable to bootstrap registry: " + id);
            }

            io.papermc.paper.registry.PaperRegistryAccess.instance().lockReferenceHolders(
            		ResourceKey.createRegistryKey(id)
            	); // Paper - lock reference holder creation
            
        });
    	
    }
    
    @Shadow
    private static void freeze() {
       /*
        * TODO: PaperRegistryListenerManager
    	REGISTRIES.freeze();
        for (Registry registry : REGISTRIES) {
            Registries.resetTagEntries(registry);
            PaperRegistryListenerManager.INSTANCE.runFreezeListeners(registry.getKey(), BUILT_IN_CONVERSIONS);
            registry.freeze();
        }
        */
    }
    
    @Shadow private static <T extends Registry<?>> void validate(Registry<T> registries) {}

	
	/**
	 * @author Cardboard
	 * @reason Implement Paper's "Add RegistryAccess for managing Registries".patch
	 */
	/*
	@Overwrite
    private static <T, R extends MutableRegistry<T>> R create(RegistryKey<? extends Registry<T>> key, R registry, Initializer<T> initializer) {
		
		Bootstrap.ensureBootstrapped(() -> "registry " + key.getValue());
		
		PaperRegistryAccess.instance().registerRegistry(registry.getKey(), registry);
		
		Identifier identifier = key.getValue();
		DEFAULT_ENTRIES.put(identifier, (Supplier)() -> initializer.run(registry));
		ROOT.add((RegistryKey<MutableRegistry<?>>)key, registry, RegistryEntryInfo.DEFAULT);
		return registry;
		
		/*
		Bootstrap.ensureBootstrapped(() -> "registry " + String.valueOf(key));
        
        // Cardboard - start
        PaperRegistryAccess.instance().registerRegistry(registry.getKey(), registry);
        // Cardboard - end
        
        Identifier resourceLocation = key.getValue();
        DEFAULT_ENTRIES.put(resourceLocation, () -> initializer.run(registry));
        ROOT.add((RegistryKey) key, registry, RegistryEntryInfo.DEFAULT);
        return registry;
        *
    }*/
   
}

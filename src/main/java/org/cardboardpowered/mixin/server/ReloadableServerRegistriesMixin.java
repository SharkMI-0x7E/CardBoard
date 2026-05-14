package org.cardboardpowered.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import com.google.gson.JsonElement;
import com.mojang.serialization.Lifecycle;

import io.papermc.paper.registry.PaperRegistryAccess;
import io.papermc.paper.registry.PaperRegistryListenerManager;
import io.papermc.paper.registry.data.util.Conversions;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.level.storage.loot.LootDataType;

@Mixin(ReloadableServerRegistries.class)
public class ReloadableServerRegistriesMixin {

	// @Shadow
    // private static final Gson GSON = new GsonBuilder().create();
	
	@Shadow
    private static final RegistrationInfo DEFAULT_REGISTRATION_INFO = new RegistrationInfo(Optional.empty(), Lifecycle.experimental());
	
	/**
	 * @author Cardboard
	 * @reason Implement Paper's "Add RegistryAccess for managing Registries".patch
	 */
    @SuppressWarnings("unchecked")
    @Overwrite
	private static <T> CompletableFuture<WritableRegistry<?>> scheduleRegistryLoad(LootDataType<T> type, RegistryOps<JsonElement> ops, ResourceManager resourceManager, Executor prepareExecutor) {
        return CompletableFuture.supplyAsync(() -> {
            MappedRegistry writableRegistry = new MappedRegistry(type.registryKey(), Lifecycle.experimental());
            PaperRegistryAccess.instance().registerReloadableRegistry(type.registryKey(), writableRegistry);
            HashMap<Identifier, T> map = new HashMap<Identifier, T>();
            SimpleJsonResourceReloadListener.scanDirectory(resourceManager, type.registryKey(), ops, type.codec(), map);
           
            // TODO Paper has conversions in reload instead of prepare
            Conversions conversions = new Conversions(ops.lookupProvider);
            
            map.forEach((id, value) -> PaperRegistryListenerManager.INSTANCE.registerWithListeners(writableRegistry, ResourceKey.create(type.registryKey(), id), value, DEFAULT_REGISTRATION_INFO, conversions));
            // TODO
            // TagGroupLoader.loadTagsForRegistry(resourceManager, writableRegistry, ReloadableRegistrarEvent.Cause.RELOAD);
            
            TagLoader.loadTagsForRegistry(resourceManager, writableRegistry);
            
            return writableRegistry;
        }, prepareExecutor);
    }

	/*
    private static <T> CompletableFuture<MutableRegistry<?>> prepare(LootDataType<T> type, RegistryOps<JsonElement> ops, ResourceManager resourceManager, Executor prepareExecutor) {
        return CompletableFuture.supplyAsync(() -> {
            SimpleRegistry writableRegistry = new SimpleRegistry(type.registryKey(), Lifecycle.experimental());
            PaperRegistryAccess.instance().registerReloadableRegistry(type.registryKey(), writableRegistry);
            HashMap<Identifier, T> map = new HashMap<Identifier, T>();
            String string = RegistryKeys.getPath(type.registryKey());
            // JsonDataLoader.load(resourceManager, string, GSON, map);
            JsonDataLoader.load(resourceManager, type.registryKey(), ops, type.codec(), map);
            
            map.forEach((id, json) -> type.parse((Identifier)id, ops, json).ifPresent(value -> writableRegistry.add(RegistryKey.of(type.registryKey(), id), value, DEFAULT_REGISTRY_ENTRY_INFO)));
            return writableRegistry;
        }, prepareExecutor);
    }
    */
	
}

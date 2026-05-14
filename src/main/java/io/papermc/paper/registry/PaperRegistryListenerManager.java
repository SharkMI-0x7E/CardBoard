package io.papermc.paper.registry;

// import io.papermc.paper.plugin.entrypoint.Entrypoint;
// import io.papermc.paper.plugin.entrypoint.LaunchEntryPointHandler;
// import io.papermc.paper.plugin.lifecycle.event.LifecycleEventRunner;
import io.papermc.paper.registry.data.util.Conversions;
import io.papermc.paper.registry.entry.RegistryEntry;
import io.papermc.paper.registry.entry.RegistryEntryInfo;
import io.papermc.paper.registry.entry.RegistryEntryMeta;
// import io.papermc.paper.registry.event.RegistryEntryAddEventImpl;
// import io.papermc.paper.registry.event.RegistryEventMap;
// import io.papermc.paper.registry.event.RegistryFreezeEvent;
// import io.papermc.paper.registry.event.RegistryFreezeEventImpl;
// import io.papermc.paper.registry.event.type.RegistryEntryAddEventTypeImpl;
// import io.papermc.paper.registry.event.type.RegistryLifecycleEventType;
        import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.bukkit.Keyed;
import org.cardboardpowered.Registries_Bridge;
import org.cardboardpowered.bridge.core.MappedRegistryBridge;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PaperRegistryListenerManager {
    public static final PaperRegistryListenerManager INSTANCE = new PaperRegistryListenerManager();
    
    // public final RegistryEventMap valueAddEventTypes = new RegistryEventMap("value add");
    // public final RegistryEventMap freezeEventTypes = new RegistryEventMap("freeze");

    private PaperRegistryListenerManager() {
    }

    public <M> M registerWithListeners(Registry<M> registry, String id, M nms) {
        return this.registerWithListeners(registry, Identifier.withDefaultNamespace(id), nms);
    }

    public <M> M registerWithListeners(Registry<M> registry, Identifier loc, M nms) {
        return this.registerWithListeners(registry, ResourceKey.create(registry.key(), loc), nms);
    }

    public <M> M registerWithListeners(Registry<M> registry, ResourceKey<M> key, M nms) {
        return (M)this.registerWithListeners(registry, key, nms, net.minecraft.core.RegistrationInfo.BUILT_IN, PaperRegistryListenerManager::registerWithInstance, Registries_Bridge.BUILT_IN_CONVERSIONS);
    }

    public <M> net.minecraft.core.Holder.Reference<M> registerForHolderWithListeners(Registry<M> registry, Identifier loc, M nms) {
        return this.registerForHolderWithListeners(registry, ResourceKey.create(registry.key(), loc), nms);
    }

    public <M> net.minecraft.core.Holder.Reference<M> registerForHolderWithListeners(Registry<M> registry, ResourceKey<M> key, M nms) {
        return this.registerWithListeners(registry, key, nms, net.minecraft.core.RegistrationInfo.BUILT_IN, WritableRegistry::register, Registries_Bridge.BUILT_IN_CONVERSIONS);
    }

    public <M> void registerWithListeners(Registry<M> registry, ResourceKey<M> key, M nms, net.minecraft.core.RegistrationInfo registrationInfo, Conversions conversions) {
        this.registerWithListeners(registry, key, nms, registrationInfo, WritableRegistry::register, conversions);
    }
    
    

    public <M, T extends Keyed, B extends PaperRegistryBuilder<M, T>, R> R registerWithListeners(
    		Registry<M> registry,
    		ResourceKey<M> key,
    		M nms,
    		net.minecraft.core.RegistrationInfo registrationInfo,
    		RegisterMethod<M, R> registerMethod,
    		Conversions conversions
    	) {
        // Preconditions.checkState((boolean)LaunchEntryPointHandler.INSTANCE.hasEntered(Entrypoint.BOOTSTRAPPER), (Object)(String.valueOf(registry.getKey()) + " tried to run modification listeners before bootstrappers have been called"));
        
        @Nullable RegistryEntry<M, T> entry = PaperRegistries.getEntry(registry.key());
        // if (!RegistryEntry.Modifiable.isModifiable(entry) || !this.valueAddEventTypes.hasHandlers(entry.apiKey())) {
            return (R) registerMethod.register((WritableRegistry)registry, key, nms, registrationInfo);
        // }
        // RegistryEntry.Modifiable modifiableEntry = RegistryEntry.Modifiable.asModifiable(entry);
        // TypedKey typedKey = TypedKey.create(entry.apiKey(), (Key)Key.key((String)key.getValue().getNamespace(), (String)key.getValue().getPath()));
        // B builder = (B) modifiableEntry.fillBuilder(conversions, typedKey, nms);
        // return (R) this.registerWithListeners(registry, modifiableEntry, key, nms, builder, registrationInfo, registerMethod, conversions);
    }
    
    <M, T extends Keyed, B extends PaperRegistryBuilder<M, T>> void registerWithListeners( // TODO remove Keyed
            final WritableRegistry<M> registry,
            final RegistryEntryMeta.Buildable<M, T, B> entry,
            final ResourceKey<M> key,
            final B builder,
            final net.minecraft.core.RegistrationInfo registrationInfo,
            final Conversions conversions
        ) {
            if (!entry.modificationApiSupport().canModify() /*|| !this.valueAddEventTypes.hasHandlers(entry.apiKey())*/) {
                registry.register(key, builder.build(), registrationInfo);
                return;
            }
            this.registerWithListeners(registry, entry, key, null, builder, registrationInfo, WritableRegistry::register, conversions);
        }
    
    
    
    

    <M, T extends Keyed, B extends PaperRegistryBuilder<M, T>> void registerWithListeners(WritableRegistry<M> registry, RegistryEntryInfo<M, T> entry, ResourceKey<M> key, B builder, net.minecraft.core.RegistrationInfo registrationInfo, Conversions conversions) {
        // if (!RegistryEntry.Modifiable.isModifiable(entry) || !this.valueAddEventTypes.hasHandlers(entry.apiKey())) {
            registry.register(key, builder.build(), registrationInfo);
            return;
        // }
        // this.registerWithListeners(registry, RegistryEntry.Modifiable.asModifiable(entry), key, null, builder, registrationInfo, MutableRegistry::add, conversions);
    }

    /*
    public <M, T extends Keyed, B extends PaperRegistryBuilder<M, T>, R> R registerWithListeners(Registry<M> registry, RegistryEntry.Modifiable<M, T, B> entry, RegistryKey<M> key, @Nullable M oldNms, B builder, net.minecraft.registry.entry.RegistryEntryInfo registrationInfo, RegisterMethod<M, R> registerMethod, Conversions conversions) {
        M newNms = oldNms;
        
        return (R) registerMethod.register((MutableRegistry)registry, key, newNms, registrationInfo);
    }
    */
    
    public <M, T extends Keyed, B extends PaperRegistryBuilder<M, T>, R> R registerWithListeners( // TODO remove Keyed
            final Registry<M> registry,
            final RegistryEntryMeta.Buildable<M, T, B> entry,
            final ResourceKey<M> key,
            final @Nullable M oldNms,
            final B builder,
            net.minecraft.core.RegistrationInfo registrationInfo,
            final RegisterMethod<M, R> registerMethod,
            final Conversions conversions
        ) {
            // @Subst("namespace:key") final Identifier beingAdded = key.getValue();
            // @SuppressWarnings("PatternValidation") final TypedKey<T> typedKey = TypedKey.create(entry.apiKey(), Key.key(beingAdded.getNamespace(), beingAdded.getPath()));
            // final RegistryEntryAddEventImpl<T, B> event = entry.createEntryAddEvent(typedKey, builder, conversions);
            // LifecycleEventRunner.INSTANCE.callEvent(this.valueAddEventTypes.getEventType(entry.apiKey()), event);
        

    	if (oldNms != null) {
                ((MappedRegistryBridge<M>) registry).clearIntrusiveHolder(oldNms);
            }
            final M newNms = oldNms; // event.builder().build();
            /*
            if (oldNms != null && !newNms.equals(oldNms)) {
                registrationInfo = new RegistryEntryInfo(Optional.empty(), Lifecycle.experimental());
            }
            */
            return registerMethod.register((WritableRegistry<M>) registry, key, newNms, registrationInfo);
        }

    private static <M> M registerWithInstance(WritableRegistry<M> writableRegistry, ResourceKey<M> key, M value, net.minecraft.core.RegistrationInfo registrationInfo) {
        writableRegistry.register(key, value, registrationInfo);
        return value;
    }

    public <M, T extends Keyed, B extends PaperRegistryBuilder<M, T>> void runFreezeListeners(ResourceKey<? extends Registry<M>> resourceKey, Conversions conversions) {
        /*
    	@Nullable RegistryEntry<M, T> entry = PaperRegistries.getEntry(resourceKey);
        if (!RegistryEntry.Addable.isAddable(entry) || !this.freezeEventTypes.hasHandlers(entry.apiKey())) {
            return;
        }
        RegistryEntry.Addable writableEntry = RegistryEntry.Addable.asAddable(entry);
        WritableCraftRegistry writableRegistry = PaperRegistryAccess.instance().getWritableRegistry(entry.apiKey());
        RegistryFreezeEventImpl event = writableEntry.createFreezeEvent(writableRegistry, conversions);
        LifecycleEventRunner.INSTANCE.callEvent(this.freezeEventTypes.getEventType(entry.apiKey()), event);
        */
    }

    /*
    public <T, B extends RegistryBuilder<T>> RegistryEntryAddEventType<T, B> getRegistryValueAddEventType(RegistryEventProvider<T, B> type) {
        if (!RegistryEntry.Modifiable.isModifiable(PaperRegistries.getEntry(type.registryKey()))) {
            throw new IllegalArgumentException(String.valueOf(type.registryKey()) + " does not support RegistryEntryAddEvent");
        }
        return this.valueAddEventTypes.getOrCreate(type.registryKey(), RegistryEntryAddEventTypeImpl::new);
    }
    *

    public <T, B extends RegistryBuilder<T>> LifecycleEventType.Prioritizable<BootstrapContext, RegistryFreezeEvent<T, B>> getRegistryFreezeEventType(RegistryEventProvider<T, B> type) {
        if (!RegistryEntry.Addable.isAddable(PaperRegistries.getEntry(type.registryKey()))) {
            throw new IllegalArgumentException(String.valueOf(type.registryKey()) + " does not support RegistryFreezeEvent");
        }
  
        CardboardMod.LOGGER.info("Debug: Crap.");
        return null;
        // return this.freezeEventTypes.getOrCreate(type.registryKey(), RegistryLifecycleEventType::new);
    }
    */
    
    /*
    public <T, B extends RegistryBuilder<T>> LifecycleEventType.Prioritizable<BootstrapContext, RegistryFreezeEvent<T, B>> getRegistryFreezeEventType(final RegistryEventProvider<T, B> type) {
        final RegistryEntry<?, ?> entry = PaperRegistries.getEntry(type.registryKey());
        if (entry == null || !entry.meta().modificationApiSupport().canAdd()) {
            throw new IllegalArgumentException(type.registryKey() + " does not support RegistryFreezeEvent");
        }
        CardboardMod.LOGGER.info("Debug: Crap.");
        return null;
        //return this.freezeEventTypes.getOrCreate(type.registryKey(), RegistryLifecycleEventType::new);
    }
    */

    @FunctionalInterface
    public static interface RegisterMethod<M, R> {
        public R register(WritableRegistry<M> var1, ResourceKey<M> var2, M var3, net.minecraft.core.RegistrationInfo var4);
    }
    
    
    
}

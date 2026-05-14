package io.papermc.paper.registry.entry;

import io.papermc.paper.registry.PaperRegistryBuilder;
import io.papermc.paper.registry.RegistryHolder;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.WritableCraftRegistry;
import io.papermc.paper.registry.data.util.Conversions;
// TODO import io.papermc.paper.registry.event.RegistryEntryAddEventImpl;
// TODO import io.papermc.paper.registry.event.RegistryFreezeEventImpl;
import io.papermc.paper.registry.legacy.DelayedRegistryEntry;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.ApiVersion;
import org.jspecify.annotations.Nullable;

import static io.papermc.paper.registry.entry.RegistryEntryBuilder.start;


public interface RegistryEntry<M, A extends Keyed> { // TODO remove Keyed

    RegistryHolder<A> createRegistryHolder(Registry<M> nmsRegistry);

    RegistryEntryMeta<M, A> meta();

    default RegistryKey<A> apiKey() {
        return this.meta().apiKey();
    }

    default net.minecraft.resources.ResourceKey<? extends Registry<M>> mcKey() {
        return this.meta().mcKey();
    }

    /**
     * This should only be used if the registry instance needs to exist early due to the need
     * to populate a field in {@link org.bukkit.Registry}. Data-driven registries shouldn't exist
     * as fields, but instead be obtained via {@link io.papermc.paper.registry.RegistryAccess#getRegistry(RegistryKey)}
     */
    @Deprecated
    default RegistryEntry<M, A> delayed() {
        return new DelayedRegistryEntry<>(this);
    }
}

/*

public interface RegistryEntry<M, B extends Keyed> extends RegistryEntryInfo<M, B> { // TODO remove Keyed

    RegistryHolder<B> createRegistryHolder(Registry<M> nmsRegistry);
    
    RegistryEntryMeta<M, B> meta();
    
    default RegistryKey<B> apiKey() {
        return this.meta().apiKey();
    }

    default net.minecraft.registry.RegistryKey<? extends Registry<M>> mcKey() {
        return this.meta().mcKey();
    }

    default RegistryEntry<M, B> withSerializationUpdater(final BiFunction<NamespacedKey, ApiVersion, NamespacedKey> updater) {
        return this;
    }

    /**
     * This should only be used if the registry instance needs to exist early due to the need
     * to populate a field in {@link org.bukkit.Registry}. Data-driven registries shouldn't exist
     * as fields, but instead be obtained via {@link io.papermc.paper.registry.RegistryAccess#getRegistry(RegistryKey)}
     *
    @Deprecated
    default RegistryEntry<M, B> delayed() {
        return new DelayedRegistryEntry<>(this);
    }

    interface BuilderHolder<M, T, B extends PaperRegistryBuilder<M, T>> extends RegistryEntryInfo<M, T> {

        B fillBuilder(Conversions conversions, M nms);
    }

    /**
     * Can mutate values being added to the registry
     *
    interface Modifiable<M, T, B extends PaperRegistryBuilder<M, T>> extends BuilderHolder<M, T, B> {

        static boolean isModifiable(final @Nullable RegistryEntryInfo<?, ?> entry) {
            return entry instanceof RegistryEntry.Modifiable<?, ?, ?> || (entry instanceof final DelayedRegistryEntry<?, ?> delayed && delayed.delegate() instanceof RegistryEntry.Modifiable<?, ?, ?>);
        }

        static <M, T extends Keyed, B extends PaperRegistryBuilder<M, T>> Modifiable<M, T, B> asModifiable(final RegistryEntryInfo<M, T> entry) { // TODO remove Keyed
            return (Modifiable<M, T, B>) possiblyUnwrap(entry);
        }

        /*
        default RegistryEntryAddEventImpl<T, B> createEntryAddEvent(final TypedKey<T> key, final B initialBuilder, final Conversions conversions) {
            return new RegistryEntryAddEventImpl<>(key, initialBuilder, this.apiKey(), conversions);
        }
        *
    }

    /**
     * Can only add new values to the registry, not modify any values.
     *
    interface Addable<M, T extends Keyed, B extends PaperRegistryBuilder<M, T>> extends BuilderHolder<M, T, B> { // TODO remove Keyed

    	/*
        default RegistryFreezeEventImpl<T, B> createFreezeEvent(final WritableCraftRegistry<M, T, B> writableRegistry, final Conversions conversions) {
            return new RegistryFreezeEventImpl<>(this.apiKey(), writableRegistry.createApiWritableRegistry(conversions), conversions);
        }
        *

        static boolean isAddable(final @Nullable RegistryEntryInfo<?, ?> entry) {
            return entry instanceof RegistryEntry.Addable<?, ?, ?> || (entry instanceof final DelayedRegistryEntry<?, ?> delayed && delayed.delegate() instanceof RegistryEntry.Addable<?, ?, ?>);
        }

        static <M, T extends Keyed, B extends PaperRegistryBuilder<M, T>> Addable<M, T, B> asAddable(final RegistryEntryInfo<M, T> entry) {
            return (Addable<M, T, B>) possiblyUnwrap(entry);
        }
    }

    /**
     * Can mutate values and add new values.
     *
    interface Writable<M, T extends Keyed, B extends PaperRegistryBuilder<M, T>> extends Modifiable<M, T, B>, Addable<M, T, B> { // TODO remove Keyed

        static boolean isWritable(final @Nullable RegistryEntryInfo<?, ?> entry) {
            return entry instanceof RegistryEntry.Writable<?, ?, ?> || (entry instanceof final DelayedRegistryEntry<?, ?> delayed && delayed.delegate() instanceof RegistryEntry.Writable<?, ?, ?>);
        }

        static <M, T extends Keyed, B extends PaperRegistryBuilder<M, T>> Writable<M, T, B> asWritable(final RegistryEntryInfo<M, T> entry) { // TODO remove Keyed
            return (Writable<M, T, B>) possiblyUnwrap(entry);
        }
    }

    private static <M, B extends Keyed> RegistryEntryInfo<M, B> possiblyUnwrap(final RegistryEntryInfo<M, B> entry) {
        return entry instanceof final DelayedRegistryEntry<M, B> delayed ? delayed.delegate() : entry;
    }

    @Deprecated
    public static <M, B extends Keyed> RegistryEntry<M, B> apiOnly(
    		net.minecraft.registry.RegistryKey<? extends Registry<M>> mcKey,
    				RegistryKey<B> apiKey,
    				Supplier<org.bukkit.Registry<B>> apiRegistrySupplier) {
        return new ApiRegistryEntry(mcKey, apiKey, apiRegistrySupplier);
    }
    
    @Deprecated
    public static <M, B extends Keyed> RegistryEntry<M, B> entry(
    		net.minecraft.registry.RegistryKey<? extends Registry<M>> aa,
    				RegistryKey<B> bb,
    				Class<?> cc,
    				BiFunction<NamespacedKey, M, B> dd) {

        return start(aa, bb).craft(cc, dd).build();
	}

    @Deprecated
	public static <M, B extends Keyed, T extends PaperRegistryBuilder<M, B>> RegistryEntry<M, B> writable(net.minecraft.registry.RegistryKey<Registry<M>> a,
			RegistryKey<B> b, Class<?> c, BiFunction<NamespacedKey, M, B> d,
			io.papermc.paper.registry.PaperRegistryBuilder.Filler<M, B, T> e) {
		// TODO Auto-generated method stub
		
        return start(a, b).craft(c, d).writable(e);
	}

}
*/

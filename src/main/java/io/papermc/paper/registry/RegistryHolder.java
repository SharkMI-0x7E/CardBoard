package io.papermc.paper.registry;

import com.google.common.base.Suppliers;
import io.papermc.paper.registry.legacy.DelayedRegistry;
import io.papermc.paper.registry.legacy.DelayedRegistryEntry;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import org.bukkit.Keyed;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(value=NonNull.class)
public interface RegistryHolder<B extends Keyed> {

    public org.bukkit.Registry<B> get();

    public static final class Delayed<B extends Keyed, R extends org.bukkit.Registry<B>>
    implements RegistryHolder<B> {
        private final DelayedRegistry<B, R> delayedRegistry = new DelayedRegistry();

        public DelayedRegistry<B, R> get() {
            return this.delayedRegistry;
        }

        <M> void loadFrom(DelayedRegistryEntry<M, B> delayedEntry, Registry<M> registry) {
            RegistryHolder<B> delegateHolder = delayedEntry.delegate().createRegistryHolder(registry);
            if (!(delegateHolder instanceof Memoized)) {
                throw new IllegalArgumentException(String.valueOf(delegateHolder) + " must be a memoized holder");
            }
            this.delayedRegistry.load(((Memoized)delegateHolder).memoizedSupplier);
        }
    }

    public static final class Memoized<B extends Keyed, R extends org.bukkit.Registry<B>> implements RegistryHolder<B> {

        private final Supplier<R> memoizedSupplier;

        public Memoized(Supplier<? extends R> supplier) {
        	this.memoizedSupplier = Suppliers.memoize(supplier::get);
        }

        @Override
        public org.bukkit.Registry<B> get() {
            return (org.bukkit.Registry)this.memoizedSupplier.get();
        }

    }

}


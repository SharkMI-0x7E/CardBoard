package io.papermc.paper.registry.legacy;

import io.papermc.paper.registry.RegistryHolder;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.entry.RegistryEntry;
import io.papermc.paper.registry.entry.RegistryEntryMeta;
import net.minecraft.core.Registry;
import org.bukkit.Keyed;

// @com.github.bsideup.jabel.Desugar
public record DelayedRegistryEntry<M, T extends Keyed>(RegistryEntry<M, T> delegate) implements RegistryEntry<M, T>
{
    @Override
    public net.minecraft.resources.ResourceKey<? extends Registry<M>> mcKey() {
        return this.delegate.mcKey();
    }

    @Override
    public RegistryKey<T> apiKey() {
        return this.delegate.apiKey();
    }

    @Override
    public RegistryHolder<T> createRegistryHolder(Registry<M> nmsRegistry) {
        return this.delegate.createRegistryHolder(nmsRegistry);
    }
    
    @Override
    public RegistryEntryMeta<M, T> meta() {
        return this.delegate.meta();
    }

}


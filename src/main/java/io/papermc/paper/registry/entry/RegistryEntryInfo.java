package io.papermc.paper.registry.entry;

import io.papermc.paper.registry.RegistryKey;
import net.minecraft.core.Registry;

public interface RegistryEntryInfo<M, B> {

    public net.minecraft.resources.ResourceKey<? extends Registry<M>> mcKey();

    public RegistryKey<B> apiKey();

}
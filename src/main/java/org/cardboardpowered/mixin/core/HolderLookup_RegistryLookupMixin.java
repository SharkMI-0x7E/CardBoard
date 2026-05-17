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
package org.cardboardpowered.mixin.core;

import java.util.Optional;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import org.cardboardpowered.bridge.core.RegistryLookupBridge;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HolderLookup.RegistryLookup.class)
public interface HolderLookup_RegistryLookupMixin<T> extends RegistryLookupBridge<T> {

	/**
	 */
	@Override
	public Optional<T> getValueForCopying(ResourceKey<T> var1);
	
	/**
	 * @author Cardboard
	 * @reason getValueForCopying
	 */
	/*
	@Overwrite(remap = false)
	default public Impl<T> method_56882(Predicate<T> predicate) {
        return new Impl.Delegating<T>(){

            // @Override
            public Optional<T> getValueForCopying(RegistryKey<T> resourceKey) {
                return ( (IRegistryWrapperImpl) this.getBase() ) .getValueForCopying(resourceKey).filter(predicate);
            }

            @Override
            public Impl<T> getBase() {
                return this;
            }

            @Override
            public Optional<RegistryEntry.Reference<T>> getOptional(RegistryKey<T> key) {
                return this.getBase().getOptional(key).filter(entry -> predicate.test(entry.value()));
            }

            @Override
            public Stream<RegistryEntry.Reference<T>> streamEntries() {
                return this.getBase().streamEntries().filter(entry -> predicate.test(entry.value()));
            }
        };
    }
    */
}

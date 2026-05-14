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

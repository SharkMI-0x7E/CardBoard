package org.cardboardpowered.bridge.core;

import java.util.Optional;
import net.minecraft.resources.ResourceKey;

public interface RegistryLookupBridge<T> {

	/**
	 */
	public Optional<T> getValueForCopying(ResourceKey<T> var1);

}

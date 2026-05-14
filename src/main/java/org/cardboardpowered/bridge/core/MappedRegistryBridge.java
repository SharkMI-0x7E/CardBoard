package org.cardboardpowered.bridge.core;

import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public interface MappedRegistryBridge<T> {

	void clearIntrusiveHolder(T instance);

	Map<Identifier, T> cb$temporaryUnfrozenMap();

	/**
	 */
	Optional<T> getValueForCopying(ResourceKey<T> resourceKey);

}

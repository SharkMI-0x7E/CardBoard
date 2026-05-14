package org.cardboardpowered.bridge.bukkit.entity;

import org.bukkit.NamespacedKey;

public interface BukkitEntityTypeBridge {

	/**
	 */
	void cardboard$setKey(NamespacedKey newKey);

	/**
	 */
	void cardboard$addToMaps(String key1, int key2);

}

/**
 * Cardboard - The Bukkit for Fabric Project
 * Copyright (C) 2020-2025 Isaiah and contributors
 */
package org.cardboardpowered.bridge.world.entity;

import org.cardboardpowered.mixin.world.entity.ItemBasedSteeringMixin;

/**
 * Injection Interface for SaddledComponent.
 * 
 * @see {@link ItemBasedSteeringMixin}
 */
public interface ItemBasedSteeringBridge {

	/**
	 */
    void setBoostTicks(int ticks);

}
package org.cardboardpowered.bridge.advancements;

import org.bukkit.advancement.Advancement;
import org.bukkit.craftbukkit.advancement.CraftAdvancement;

/**
 * 
 */
public interface AdvancementHolderBridge {

    CraftAdvancement getBukkitAdvancement();

	Advancement toBukkit();

}
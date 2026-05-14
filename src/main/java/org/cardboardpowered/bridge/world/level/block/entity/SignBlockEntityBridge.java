package org.cardboardpowered.bridge.world.level.block.entity;

import net.minecraft.network.chat.Component;

/**
 */
public interface SignBlockEntityBridge {

	/**
	 */
    Component[] getTextBF();

    /**
     * Note: bukkit adds method.
     */
	boolean cardboard$isFacingFrontText(double x, double z);

}
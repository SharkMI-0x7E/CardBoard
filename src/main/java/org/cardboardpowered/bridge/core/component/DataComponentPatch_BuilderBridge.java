package org.cardboardpowered.bridge.core.component;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;

public interface DataComponentPatch_BuilderBridge {
 
	void copy(DataComponentPatch orig);

	void clear(DataComponentType<?> type);

	boolean isSet(DataComponentType<?> type);

	boolean isEmpty();
}

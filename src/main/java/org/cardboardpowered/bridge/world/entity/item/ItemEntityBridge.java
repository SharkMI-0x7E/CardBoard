package org.cardboardpowered.bridge.world.entity.item;

public interface ItemEntityBridge {

	int cardboard$getHealth();

	void cardboard$setHealth(int newVal);

	int cardboard$itemAge();

	void cardboard$setUnlimitedAge(boolean noLimit);

	void cardboard$setItemAge(int value);

}

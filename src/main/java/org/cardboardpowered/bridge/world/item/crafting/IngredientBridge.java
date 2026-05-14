/**
 * Cardboard - Bukkit for Fabric Project
 * Copyright (C) 2020-2025 Cardboard contributors
 */
package org.cardboardpowered.bridge.world.item.crafting;

import java.util.List;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public interface IngredientBridge {

    boolean getExact_BF();

    void setExact_BF(boolean value);

	void cardboard$set_itemStacks(List<ItemStack> stacks);

	static Ingredient cb$ofStacks(List<ItemStack> stacks) {
		Ingredient recipe = Ingredient.of(stacks.stream().map(ItemStack::getItem));
		((IngredientBridge)recipe).cardboard$set_itemStacks(stacks);
		return recipe;
	}

	boolean cb$isExact();

	HolderSet<Item> cb$entries();

	List<ItemStack> cb$itemStacks();

}
/**
 * Copyright (C) 2026 SharkMI and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 */
/**
 */
package org.cardboardpowered.bridge.world.item.crafting;

import net.minecraft.resources.ResourceKey;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftRecipe;

import com.google.common.collect.Multimap;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

public interface RecipeManagerBridge {

    default void addRecipe(NamespacedKey key, Recipe<?> recipe) {
        cardboard$addRecipe(new RecipeHolder<>(
        		CraftRecipe.toMinecraft(key),
                recipe
        ));
    }

    void cardboard$addRecipe(RecipeHolder<?> recipeEntry);

    void cardboard$clearRecipes();

    void cardboard$finalizeRecipeLoading();

    boolean cardboard$removeRecipe(ResourceKey<Recipe<?>> mcKey);

	Multimap<RecipeType<?>, RecipeHolder<?>> cb$get_recipesByType();
}

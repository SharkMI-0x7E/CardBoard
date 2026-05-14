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

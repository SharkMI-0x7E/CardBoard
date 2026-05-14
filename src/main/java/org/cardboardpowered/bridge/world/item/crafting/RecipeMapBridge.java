package org.cardboardpowered.bridge.world.item.crafting;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;

public interface RecipeMapBridge {
    void cardboard$addRecipe(RecipeHolder<?> holder);

    <T extends RecipeInput> boolean cardboard$removeRecipe(ResourceKey<Recipe<T>> mcKey);
}

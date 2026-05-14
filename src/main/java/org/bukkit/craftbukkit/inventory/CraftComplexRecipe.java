package org.bukkit.craftbukkit.inventory;

import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.inventory.ComplexRecipe;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.cardboardpowered.bridge.world.item.crafting.RecipeManagerBridge;

public class CraftComplexRecipe extends CraftingRecipe implements CraftRecipe, ComplexRecipe {

    private final CustomRecipe recipe;

    public CraftComplexRecipe(NamespacedKey key, ItemStack result, CustomRecipe recipe) {
        super(key, result);
        this.recipe = recipe;
    }

    @Override
    public void addToCraftingManager() {
        ((RecipeManagerBridge)CraftServer.INSTANCE.getServer().getRecipeManager()).cardboard$addRecipe(new RecipeHolder<>(CraftRecipe.toMinecraft(this.getKey()), this.recipe));
    }
}

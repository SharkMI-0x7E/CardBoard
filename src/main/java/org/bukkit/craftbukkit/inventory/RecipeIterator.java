package org.bukkit.craftbukkit.inventory;

import java.util.Iterator;
import java.util.Map;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.inventory.Recipe;
import org.cardboardpowered.bridge.server.players.PlayerListBridge;
import org.cardboardpowered.bridge.world.item.crafting.RecipeHolderBridge;
import org.cardboardpowered.bridge.world.item.crafting.RecipeManagerBridge;

public class RecipeIterator implements Iterator<Recipe> {
    private final Iterator<Map.Entry<RecipeType<?>, RecipeHolder<?>>> recipes;
    private RecipeHolder<?> currentRecipe;

    public RecipeIterator() {
        this.recipes = CraftServer.INSTANCE.getServer().getRecipeManager().recipes.byType.entries().iterator();
    }

    @Override
    public boolean hasNext() {
        return this.recipes.hasNext();
    }

    @Override
    public Recipe next() {
        this.currentRecipe = this.recipes.next().getValue();
        return ((RecipeHolderBridge)(Object)this.currentRecipe).toBukkitRecipe();
    }

    @Override
    public void remove() {
        CraftServer.INSTANCE.getServer().getRecipeManager().recipes.byKey.remove(this.currentRecipe.id());
        this.recipes.remove();
        ((RecipeManagerBridge)CraftServer.INSTANCE.getServer().getRecipeManager()).cardboard$finalizeRecipeLoading();
        ((PlayerListBridge)CraftServer.INSTANCE.getServer().getPlayerList()).cardboard$reloadRecipes();
    }
}

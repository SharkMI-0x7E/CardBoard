package org.bukkit.craftbukkit.inventory.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SelectableRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.StonecutterInventory;
import org.bukkit.inventory.StonecuttingRecipe;
import org.bukkit.inventory.view.StonecutterView;
import org.jetbrains.annotations.NotNull;

import org.cardboardpowered.bridge.world.item.crafting.RecipeHolderBridge;

public class CraftStonecutterView extends CraftInventoryView<StonecutterMenu, StonecutterInventory> implements StonecutterView {

    public CraftStonecutterView(final HumanEntity player, final StonecutterInventory viewing, final StonecutterMenu container) {
        super(player, viewing, container);
    }

    @Override
    public int getSelectedRecipeIndex() {
        return ((StonecutterMenu)this.container).getSelectedRecipeIndex();
    }
    
    // @Override
    public Recipe toBukkitRecipe(NamespacedKey id) {
        /*
    	CraftItemStack result = CraftItemStack.asCraftMirror(this.result());
        CraftStonecuttingRecipe recipe = new CraftStonecuttingRecipe(id, result, CraftRecipe.toBukkit(this.ingredient()));
        recipe.setGroup(this.getGroup());
        return recipe;
        */
    	return null;
    }

    @NotNull
    @Override
    public List<StonecuttingRecipe> getRecipes() {
        final List<StonecuttingRecipe> recipes = new ArrayList<>();
        for (final SelectableRecipe.SingleInputEntry<net.minecraft.world.item.crafting.StonecutterRecipe> recipe : ((StonecutterMenu)this.container).getVisibleRecipes().entries()) {
            
        	Optional<RecipeHolder<net.minecraft.world.item.crafting.StonecutterRecipe>> opt = recipe.recipe().recipe();
        	
        	if (opt.isPresent()) {
        		RecipeHolder<net.minecraft.world.item.crafting.StonecutterRecipe> rep = opt.get();

        		Recipe bukkit = ((RecipeHolderBridge) (Object) rep).toBukkitRecipe();
        		recipes.add((StonecuttingRecipe) bukkit);
        	}
        	
        	// recipe.recipe().recipe().map(RecipeEntry::toBukkitRecipe).ifPresent((bukkit) -> recipes.add((StonecuttingRecipe) bukkit));
        }
        return recipes;
    }

    @Override
    public int getRecipeAmount() {
        return ((StonecutterMenu)this.container).getNumberOfVisibleRecipes();
    }
}

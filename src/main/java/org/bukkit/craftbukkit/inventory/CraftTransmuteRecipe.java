package org.bukkit.craftbukkit.inventory;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.TransmuteResult;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.TransmuteRecipe;
import org.cardboardpowered.bridge.world.item.crafting.RecipeManagerBridge;

public class CraftTransmuteRecipe extends TransmuteRecipe implements CraftRecipe {

    public CraftTransmuteRecipe(NamespacedKey key, Material result, RecipeChoice input, RecipeChoice material) {
        super(key, result, input, material);
    }

    public static CraftTransmuteRecipe fromBukkitRecipe(TransmuteRecipe recipe) {
        if (recipe instanceof CraftTransmuteRecipe) {
            return (CraftTransmuteRecipe) recipe;
        }
        CraftTransmuteRecipe ret = new CraftTransmuteRecipe(recipe.getKey(), recipe.getResult().getType(), recipe.getInput(), recipe.getMaterial());
        ret.setGroup(recipe.getGroup());
        ret.setCategory(recipe.getCategory());
        return ret;
    }

    @Override
    public void addToCraftingManager() {
        final ItemStack unwrappedInternalStack = CraftItemStack.unwrap(this.getResult());
        ((RecipeManagerBridge)CraftServer.INSTANCE.getServer().getRecipeManager()).cardboard$addRecipe(
                new RecipeHolder<>(CraftRecipe.toMinecraft(this.getKey()),
                        new net.minecraft.world.item.crafting.TransmuteRecipe(this.getGroup(),
                                CraftRecipe.getCategory(this.getCategory()),
                                this.toNMS(this.getInput(), true),
                                this.toNMS(this.getMaterial(), true),
                                new TransmuteResult(unwrappedInternalStack.getItemHolder(), unwrappedInternalStack.getCount(), unwrappedInternalStack.getComponentsPatch())
                        )
                )
        );
    }
}

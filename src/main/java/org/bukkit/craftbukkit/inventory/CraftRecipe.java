package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Ingredient;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.cardboardpowered.bridge.world.item.crafting.IngredientBridge;

public interface CraftRecipe extends Recipe {

    void addToCraftingManager();

    default Optional<Ingredient> toNMSOptional(RecipeChoice bukkit, boolean requireNotEmpty) {
        return (bukkit == null || bukkit == RecipeChoice.empty()) ? Optional.empty() : Optional.of(this.toNMS(bukkit, requireNotEmpty));
    }

    default Ingredient toNMS(RecipeChoice bukkit, boolean requireNotEmpty) {
        return toIngredient(bukkit, requireNotEmpty);
    }

    static Ingredient toIngredient(RecipeChoice bukkit, boolean requireNotEmpty) {
        Ingredient stack;

        if (bukkit == null) {
            stack = Ingredient.of();
        } else if (bukkit instanceof RecipeChoice.MaterialChoice) {
            stack = Ingredient.of(((RecipeChoice.MaterialChoice) bukkit).getChoices().stream().map((mat) -> CraftItemType.bukkitToMinecraft(mat)));
        } else if (bukkit instanceof RecipeChoice.ExactChoice) {
            stack = IngredientBridge.cb$ofStacks(((RecipeChoice.ExactChoice) bukkit).getChoices().stream().map((mat) -> CraftItemStack.asNMSCopy(mat)).toList());
        } else if (bukkit == RecipeChoice.empty()) {
            throw new IllegalArgumentException("This ingredient cannot be empty");
        } else {
            throw new IllegalArgumentException("Unknown recipe stack instance " + bukkit);
        }

        if (requireNotEmpty) {
            Preconditions.checkArgument(!stack.isEmpty(), "Recipe requires at least one non-air choice");
        }

        return stack;
    }

    public static RecipeChoice toBukkit(Optional<Ingredient> list) {
        return list.map(CraftRecipe::toBukkit).orElse(RecipeChoice.empty());
    }

    public static RecipeChoice toBukkit(Ingredient list) {
        if (list.isEmpty()) {
            return RecipeChoice.empty();
        }

        IngredientBridge cblist = (IngredientBridge) list;

        if (cblist.cb$isExact()) {
            List<org.bukkit.inventory.ItemStack> choices = new ArrayList<>(cblist.cb$itemStacks().size());
            for (net.minecraft.world.item.ItemStack i : cblist.cb$itemStacks()) {
                choices.add(CraftItemStack.asBukkitCopy(i));
            }

            return new RecipeChoice.ExactChoice(choices);
        }

        List<org.bukkit.Material> choices = list.items().map((i) -> CraftItemType.minecraftToBukkit(i.value())).filter(org.bukkit.Material::isItem).toList();
        if (choices.isEmpty()) {
            return RecipeChoice.empty();
        }

        return new RecipeChoice.MaterialChoice(choices);
    }

    public static net.minecraft.world.item.crafting.CraftingBookCategory getCategory(CraftingBookCategory bukkit) {
        return net.minecraft.world.item.crafting.CraftingBookCategory.valueOf(bukkit.name());
    }

    public static CraftingBookCategory getCategory(net.minecraft.world.item.crafting.CraftingBookCategory nms) {
        return CraftingBookCategory.valueOf(nms.name());
    }

    public static net.minecraft.world.item.crafting.CookingBookCategory getCategory(CookingBookCategory bukkit) {
        return net.minecraft.world.item.crafting.CookingBookCategory.valueOf(bukkit.name());
    }

    public static CookingBookCategory getCategory(net.minecraft.world.item.crafting.CookingBookCategory nms) {
        return CookingBookCategory.valueOf(nms.name());
    }

    public static ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> toMinecraft(NamespacedKey key) {
        return ResourceKey.create(Registries.RECIPE, CraftNamespacedKey.toMinecraft(key));
    }
}
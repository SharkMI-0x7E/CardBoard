package org.cardboardpowered.mixin.world.item.crafting;

import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.CraftServer;
import org.cardboardpowered.bridge.world.item.crafting.RecipeManagerBridge;

import net.minecraft.world.flag.FeatureFlagSet;
import org.cardboardpowered.bridge.world.item.crafting.RecipeMapBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin implements RecipeManagerBridge {

    @Shadow
    private RecipeMap recipes;

    @Shadow
    public abstract void finalizeRecipeLoading(FeatureFlagSet featureFlagSet);

    @Override
	public  Multimap<RecipeType<?>, RecipeHolder<?>> cb$get_recipesByType() {
		return null; // TODO return recipesByType;
	}

    @Unique
    private FeatureFlagSet featureflagset;

    // CraftBukkit start
    @Override
    public void cardboard$addRecipe(RecipeHolder<?> holder) {
        org.spigotmc.AsyncCatcher.catchOp("Recipe Add"); // Spigot
        ((RecipeMapBridge)this.recipes).cardboard$addRecipe(holder);
        this.cardboard$finalizeRecipeLoading();
    }

    @Override
    public void cardboard$finalizeRecipeLoading() {
        if (this.featureflagset != null) {
            this.finalizeRecipeLoading(this.featureflagset);

            CraftServer.INSTANCE.getServer().getPlayerList().reloadResources();
        }
    }

    @Inject(method = "finalizeRecipeLoading", at = @At("HEAD"))
    public void finalizeRecipeLoadingCraftBukkit(FeatureFlagSet enabledFeatures, CallbackInfo ci) {
        this.featureflagset = enabledFeatures;
        // CraftBukkit end
    }

    @Inject(method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    public <I extends RecipeInput, T extends Recipe<I>> void getRecipeFor(RecipeType<T> recipeType, I input, Level level, CallbackInfoReturnable<Optional<RecipeHolder<T>>> cir) {
        // CraftBukkit start
        List<RecipeHolder<T>> list = this.recipes.getRecipesFor(recipeType, input, level).toList();
        cir.setReturnValue((list.isEmpty()) ? Optional.empty() : Optional.of(list.getLast())); // CraftBukkit - SPIGOT-4638: last recipe gets priority
        // CraftBukkit end
    }

    // CraftBukkit start
    @Override
    public boolean cardboard$removeRecipe(ResourceKey<Recipe<?>> mcKey) {
        boolean removed = ((RecipeMapBridge)this.recipes).cardboard$removeRecipe((ResourceKey<Recipe<RecipeInput>>) (ResourceKey) mcKey); // Paper - generic fix
        if (removed) {
            this.cardboard$finalizeRecipeLoading();
        }

        return removed;
    }

    @Override
    public void cardboard$clearRecipes() {
        this.recipes = RecipeMap.create(java.util.Collections.emptyList());
        this.cardboard$finalizeRecipeLoading();
    }
    // CraftBukkit end
}

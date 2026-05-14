package org.cardboardpowered.mixin.world.item.crafting;

import java.util.Objects;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.cardboardpowered.bridge.world.item.crafting.IngredientBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Ingredient.class)
public class IngredientMixin implements IngredientBridge {

	@Shadow
	private HolderSet<Item> values;
	
	// Paper start
	@Override
	public HolderSet<Item> cb$entries() {
		return values;
	}
	
	private java.util.List<ItemStack> itemStacks;
	
	@Override
	public boolean cb$isExact() {
        return this.itemStacks != null;
    }
	
	@Override
	public java.util.List<ItemStack> cb$itemStacks() {
		return this.itemStacks;
	}
	
	@Override
	public void cardboard$set_itemStacks(java.util.List<ItemStack> stacks) {
		this.itemStacks = stacks;
	}

	// Paper end
	
	
	// @Shadow public Entry[] entries;
    // @Shadow public ItemStack[] matchingStacks;
    // @Shadow public void cacheMatchingStacks() {}
    
    public boolean exact_BF;

    @Override
    public boolean getExact_BF() {
        return exact_BF;
    }

    @Override
    public void setExact_BF(boolean value) {
        exact_BF = value;
    }
    
    // @Shadow
    // public ItemStack[] getMatchingStacks() {
    //	return null;
    // }
    
    @Inject(method = "test(Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void banner$test(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {

    	if (exact_BF || this.cb$isExact()) {
    		for (ItemStack itemstack1 : this.cb$itemStacks()) {
    			if (itemstack1.getItem() == stack.getItem() && ItemStack.isSameItemSameComponents(stack, itemstack1)) {
    				cir.setReturnValue(true);
    				return;
    			}
    		}
    		cir.setReturnValue(false);
    		return;
    	}

    	
    	/*
    	for (ItemStack banner$stack : this.getMatchingStacks()) {
            // CraftBukkit start
            if (exact_BF) {
            	
            	// Yarn Note:
            	// >=1.20.5: areItemsAndComponentsEqual
            	// <=1.20.4: canCombine
            	
            	// if (ItemStack.canCombine(banner$stack, stack)) {
                if (ItemStack.areItemsAndComponentsEqual(banner$stack, stack)) {
                    cir.setReturnValue(true);
                }
                continue;
            }
            if (banner$stack.isOf(stack.getItem())) {
                cir.setReturnValue(true);
            }
            // CraftBukkit end
        }
        */
    }
    
    @Inject(method = "equals(Ljava/lang/Object;)Z",
            at = @At("RETURN"),
            cancellable = true)
    private void cardboard$does_ingredient_equal(Object other, CallbackInfoReturnable<Boolean> cir) {
    	if (other instanceof Ingredient ingredient) {
    		boolean paper_equals = Objects.equals(this.itemStacks, ((IngredientBridge)ingredient).cb$itemStacks());
    		if (!paper_equals) {
    			cir.setReturnValue(false);
    			return;
    		}
    	}
    }
    
    /*
    @Override
    public boolean equals(Object other) {
    	// return other instanceof Ingredient ingredient && Objects.equals(this.values, ingredient.values);
    	return other instanceof Ingredient ingredient && 
    			Objects.equals(this.cb$entries(), ingredient.cb$entries()) && Objects.equals(this.itemStacks, ingredient.itemStacks);
    }
    */
    
    /*
    public boolean test(ItemStack itemstack) {
        if (itemstack == null) {
            return false;
        } else {
            if (this.entries.length == 0) {
            	return itemstack.isEmpty();
            }

                ItemStack[] aitemstack = getMatchingStacks();
                int i = aitemstack.length;

                for (int j = 0; j < i; ++j) {
                    ItemStack itemstack1 = aitemstack[j];

                    // Bukkit start
                    if (exact_BF) {
                        if (itemstack1.getItem() == itemstack.getItem() && ItemStack.areNbtEqual(itemstack, itemstack1))
                            return true;
                        continue;
                    }
                    // Bukkit end
                    if (itemstack1.getItem() == itemstack.getItem())
                        return true;
                }

                return false;
            
        }
    }*/

}
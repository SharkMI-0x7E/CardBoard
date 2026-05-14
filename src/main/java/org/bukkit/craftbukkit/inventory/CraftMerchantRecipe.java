package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Preconditions;

import me.isaiah.common.ICommonMod;
import me.isaiah.common.cmixin.IMixinTradeOffer;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

public class CraftMerchantRecipe extends MerchantRecipe {

    private final net.minecraft.world.item.trading.MerchantOffer handle;

    public CraftMerchantRecipe(net.minecraft.world.item.trading.MerchantOffer merchantRecipe) {
        super(CraftItemStack.asBukkitCopy(merchantRecipe.result), 0);
        this.handle = merchantRecipe;
        
        IMixinTradeOffer ic = (IMixinTradeOffer) (Object) merchantRecipe;
        
        addIngredient(CraftItemStack.asBukkitCopy(ic.IC$get_first_buy_itemstack()));
        
        if (null != ic.IC$get_second_buy_itemstack()) {
        	addIngredient(CraftItemStack.asBukkitCopy(ic.IC$get_second_buy_itemstack()));
        }
    }

    public CraftMerchantRecipe(ItemStack result, int uses, int maxUses, boolean experienceReward, int experience, float priceMultiplier) {
        super(result, uses, maxUses, experienceReward, experience, priceMultiplier);
   
        this.handle = ICommonMod.getIServer().create_trade_offer(
                CraftItemStack.asNMSCopy(result),
                uses,
                maxUses,
                experienceReward,
                experience,
                priceMultiplier, 0, 0);

        this.setExperienceReward(experienceReward);
    }

    @Override
    public int getUses() {
        return handle.uses;
    }

    @Override
    public void setUses(int uses) {
        handle.uses = uses;
    }

    @Override
    public int getMaxUses() {
        return handle.maxUses;
    }

    @Override
    public void setMaxUses(int maxUses) {
        handle.maxUses = maxUses;
    }

    @Override
    public boolean hasExperienceReward() {
        return handle.rewardExp;
    }

    @Override
    public void setExperienceReward(boolean flag) {
        handle.rewardExp = flag;
    }

    @Override
    public int getVillagerExperience() {
        return handle.xp;
    }

    @Override
    public void setVillagerExperience(int villagerExperience) {
        handle.xp = villagerExperience;
    }

    @Override
    public float getPriceMultiplier() {
        return handle.priceMultiplier;
    }

    @Override
    public void setPriceMultiplier(float priceMultiplier) {
        handle.priceMultiplier = priceMultiplier;
    }

    public net.minecraft.world.item.trading.MerchantOffer toMinecraft() {
        List<ItemStack> ingredients = getIngredients();
        Preconditions.checkState(!ingredients.isEmpty(), "No offered ingredients");
        
        IMixinTradeOffer ic = (IMixinTradeOffer) (Object) handle;
        
        ic.IC$set_first_buy_itemstack( CraftItemStack.asNMSCopy(ingredients.get(0)) );
        
        if (ingredients.size() > 1) {
        	ic.IC$set_second_buy_itemstack( CraftItemStack.asNMSCopy(ingredients.get(1)));
        }

        /*handle.firstBuyItem = CraftItemStack.asNMSCopy(ingredients.get(0));
        if (ingredients.size() > 1)
            handle.secondBuyItem = CraftItemStack.asNMSCopy(ingredients.get(1));*/
        return handle;
    }

    public static CraftMerchantRecipe fromBukkit(MerchantRecipe recipe) {
        if (recipe instanceof CraftMerchantRecipe) {
            return (CraftMerchantRecipe) recipe;
        } else {
            CraftMerchantRecipe craft = new CraftMerchantRecipe(recipe.getResult(), recipe.getUses(), recipe.getMaxUses(), recipe.hasExperienceReward(), recipe.getVillagerExperience(), recipe.getPriceMultiplier());
            craft.setIngredients(recipe.getIngredients());
            return craft;
        }
    }

}
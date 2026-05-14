package org.bukkit.craftbukkit.inventory;

import net.minecraft.world.inventory.MerchantContainer;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.cardboardpowered.bridge.world.entity.npc.villager.AbstractVillagerBridge;
import org.cardboardpowered.bridge.world.item.trading.MerchantOfferBridge;

public class CraftInventoryMerchant extends CraftInventory implements MerchantInventory {

    private final net.minecraft.world.item.trading.Merchant merchant;

    public CraftInventoryMerchant(net.minecraft.world.item.trading.Merchant merchant, MerchantContainer inventory) {
        super(inventory);
        this.merchant = merchant;
    }

    @Override
    public int getSelectedRecipeIndex() {
        return this.getInventory().selectionHint;
    }

    @Override
    public MerchantRecipe getSelectedRecipe() {
        net.minecraft.world.item.trading.MerchantOffer nmsRecipe = this.getInventory().getActiveOffer();
        return (nmsRecipe == null) ? null : ((MerchantOfferBridge)nmsRecipe).asBukkit();
    }

    @Override
    public MerchantContainer getInventory() {
        return (MerchantContainer) this.inventory;
    }

    @Override
    public Merchant getMerchant() {
        return ((AbstractVillagerBridge)this.merchant).getCraftMerchant();
    }
}

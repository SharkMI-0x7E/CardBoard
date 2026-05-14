package org.bukkit.craftbukkit.inventory.view;

import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.inventory.MerchantMenu;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.view.MerchantView;
import org.bukkit.craftbukkit.entity.CraftAbstractVillager;
import org.jetbrains.annotations.NotNull;

import org.cardboardpowered.bridge.world.entity.EntityBridge;

public class CraftMerchantView extends CraftInventoryView<MerchantMenu, MerchantInventory> implements MerchantView {

    private final net.minecraft.world.item.trading.Merchant trader;

    public CraftMerchantView(final HumanEntity player, final MerchantInventory viewing, final MerchantMenu container, final net.minecraft.world.item.trading.Merchant trader) {
        super(player, viewing, container);
        this.trader = trader;
    }

    @NotNull
    @Override
    public Merchant getMerchant() {
    	
    	if (this.trader instanceof AbstractVillager) {
    		return (CraftAbstractVillager) (((EntityBridge)this.trader).getBukkitEntity());
    	}
    	
    	return null; // TODO
        // return this.trader.getCraftMerchant();
    }
}

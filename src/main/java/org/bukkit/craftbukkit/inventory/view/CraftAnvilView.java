package org.bukkit.craftbukkit.inventory.view;

import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.craftbukkit.inventory.CraftInventoryAnvil;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.inventory.AnvilMenu;

public class CraftAnvilView extends CraftInventoryView<AnvilMenu, AnvilInventory> implements AnvilView {

    public CraftAnvilView(final HumanEntity player, final AnvilInventory viewing, final AnvilMenu container) {
        super(player, viewing, container);
    }
    
    @Nullable
    @Override
    public String getRenameText() {
        return ((AnvilMenu) this.container).itemName;
    }

    @Override
    public int getRepairItemCountCost() {
        return ((AnvilMenu) this.container).repairItemCountCost;
    }

    @Override
    public int getRepairCost() {
        return ((AnvilMenu) this.container).getCost();
    }

    @Override
    public int getMaximumRepairCost() {
        return getRepairCost();
    	// return ((AnvilScreenHandler) this.container).maximumRepairCost;
    }

    @Override
    public void setRepairItemCountCost(final int cost) {
    	((AnvilMenu) this.container).repairItemCountCost = cost;
    }

    @Override
    public void setRepairCost(final int cost) {
    	((AnvilMenu) this.container).cost.set(cost);
    }

    @Override
    public void setMaximumRepairCost(final int cost) {
    	// ((AnvilScreenHandler) this.container).maximumRepairCost = cost;
    }

    // Paper start
    @Override
    public boolean bypassesEnchantmentLevelRestriction() {
        return false;
    	// return ((AnvilScreenHandler) this.container).bypassEnchantmentLevelRestriction;
    }

    @Override
    public void bypassEnchantmentLevelRestriction(final boolean bypassEnchantmentLevelRestriction) {
    	// ((AnvilScreenHandler) this.container).bypassEnchantmentLevelRestriction = bypassEnchantmentLevelRestriction;
    }
    // Paper end

    public void updateFromLegacy(CraftInventoryAnvil legacy) {
    	
    	// CraftInventoryAnvil
    	// CardboardAnvilInventory
    	
        if (legacy.isRepairCostSet()) {
            this.setRepairCost(legacy.getRepairCost());
        }

        if (legacy.isRepairCostAmountSet()) {
            this.setRepairItemCountCost(legacy.getRepairCostAmount());
        }

        if (legacy.isMaximumRepairCostSet()) {
            this.setMaximumRepairCost(legacy.getMaximumRepairCost());
        }
    }
}

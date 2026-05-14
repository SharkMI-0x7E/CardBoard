package org.cardboardpowered.mixin.world.inventory;

import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.inventory.CraftInventorySmithing;
import net.minecraft.world.inventory.SmithingMenu;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.world.inventory.ContainerLevelAccessBridge;
import org.spongepowered.asm.mixin.Mixin;

import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;

@Mixin(SmithingMenu.class)
public class SmithingMenuMixin extends ItemCombinerMenuMixin {

    private CraftInventoryView bukkitEntity;

    // CraftBukkit start
    @Override
    public org.bukkit.craftbukkit.inventory.CraftInventoryView getBukkitView() {
        if (this.bukkitEntity != null) {
            return this.bukkitEntity;
        }

        org.bukkit.craftbukkit.inventory.CraftInventory inventory = new org.bukkit.craftbukkit.inventory.CraftInventorySmithing(
                ((ContainerLevelAccessBridge)this.access).getLocation(), this.inputSlots, this.resultSlots);
        this.bukkitEntity = new org.bukkit.craftbukkit.inventory.CraftInventoryView((HumanEntity) ((EntityBridge)this.player).getBukkitEntity(), inventory, (SmithingMenu)(Object)this);
        return this.bukkitEntity;
    }
    // CraftBukkit end

}
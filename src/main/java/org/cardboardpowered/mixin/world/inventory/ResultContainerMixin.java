package org.cardboardpowered.mixin.world.inventory;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import org.cardboardpowered.bridge.world.ContainerBridge;

@Mixin(ResultContainer.class)
public abstract class ResultContainerMixin implements Container, ContainerBridge {

    @Shadow public NonNullList<ItemStack> itemStacks;

    private int maxStack = MAX_STACK;

    @Override
    public List<ItemStack> getContents() {
        return itemStacks;
    }

    // MixinCraftingInventory takes care of this
    @Override public void onOpen(CraftHumanEntity who) {}
    @Override public void onClose(CraftHumanEntity who) {}
    @Override public List<HumanEntity> getViewers() {return new ArrayList<HumanEntity>();}

    @Override
    public InventoryHolder getOwner() {
        return null; // There is no owner for for the result inventory
    }

    @Override
    public void cardboard$setMaxStackSize(int size) {
        maxStack = size;
    }

    @Override
    public Location getLocation() {
        return null; // No location for this inventory
    }

    @Override
    public int getMaxStackSize() {
        return maxStack;
    }

}
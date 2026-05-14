package org.cardboardpowered.mixin.world;

import net.minecraft.world.Container;
import org.cardboardpowered.bridge.world.ContainerBridge;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

@Mixin(SimpleContainer.class)
public abstract class SimpleContainerMixin implements Container, ContainerBridge {

    @Final @Shadow
    public NonNullList<ItemStack> items;

    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    public int maxStack_BF = MAX_STACK;
    
    public InventoryHolder bukkitOwner;
    
    @Override
    public void cardboard$setOwner(InventoryHolder owner) {
        this.bukkitOwner = owner;
    }

    @Override
    public List<ItemStack> getContents() {
        return items;
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public InventoryHolder getOwner() {
        // TODO Auto-generated method stub
        
        InventoryHolder hold = (transaction.size() >= 1) ? transaction.get(0) : null;
        if (null == hold) {
            System.out.println("NULL HOLD!");
            return this.bukkitOwner;
        }
        return hold;
    }

    @Override
    public void cardboard$setMaxStackSize(int size) {
        maxStack_BF = size;
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public int getMaxStackSize() {
        return maxStack_BF;
    }

}

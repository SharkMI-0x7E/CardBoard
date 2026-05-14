package org.bukkit.craftbukkit.inventory;

import java.util.Arrays;
import java.util.List;
import net.minecraft.world.Container;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import org.cardboardpowered.bridge.world.ContainerBridge;
import org.cardboardpowered.bridge.world.item.crafting.RecipeHolderBridge;

public class CraftInventoryCrafting extends CraftInventory implements CraftingInventory {
    private final Container resultInventory;

    public CraftInventoryCrafting(Container inventory, Container resultInventory) {
        super(inventory);
        this.resultInventory = resultInventory;
    }

    public Container getResultInventory() {
        return resultInventory;
    }

    public Container getMatrixInventory() {
        return inventory;
    }

    @Override
    public int getSize() {
        return getResultInventory().getContainerSize() + getMatrixInventory().getContainerSize();
    }

    @Override
    public void setContents(ItemStack[] items) {
        if (getSize() > items.length)
            throw new IllegalArgumentException("Invalid inventory size; expected " + getSize() + " or less");
        setContents(items[0], Arrays.copyOfRange(items, 1, items.length));
    }

    @Override
    public ItemStack[] getContents() {
        ItemStack[] items = new ItemStack[getSize()];
        List<net.minecraft.world.item.ItemStack> mcResultItems = ((ContainerBridge)getResultInventory()).getContents();

        int i = 0;
        for (i = 0; i < mcResultItems.size(); i++)
            items[i] = CraftItemStack.asCraftMirror(mcResultItems.get(i));

        List<net.minecraft.world.item.ItemStack> mcItems = ((ContainerBridge)getMatrixInventory()).getContents();
        for (int j = 0; j < mcItems.size(); j++)
            items[i + j] = CraftItemStack.asCraftMirror(mcItems.get(j));

        return items;
    }

    public void setContents(ItemStack result, ItemStack[] contents) {
        setResult(result);
        setMatrix(contents);
    }

    @Override
    public CraftItemStack getItem(int index) {
        if (index < getResultInventory().getContainerSize()) {
            net.minecraft.world.item.ItemStack item = getResultInventory().getItem(index);
            return item.isEmpty() ? null : CraftItemStack.asCraftMirror(item);
        } else {
            net.minecraft.world.item.ItemStack item = getMatrixInventory().getItem(index - getResultInventory().getContainerSize());
            return item.isEmpty() ? null : CraftItemStack.asCraftMirror(item);
        }
    }

    @Override
    public void setItem(int index, ItemStack item) {
        if (index < getResultInventory().getContainerSize())
            getResultInventory().setItem(index, CraftItemStack.asNMSCopy(item));
        else getMatrixInventory().setItem((index - getResultInventory().getContainerSize()), CraftItemStack.asNMSCopy(item));
    }

    @Override
    public ItemStack[] getMatrix() {
        return asCraftMirror(((ContainerBridge)getMatrixInventory()).getContents());
    }

    @Override
    public ItemStack getResult() {
        net.minecraft.world.item.ItemStack item = getResultInventory().getItem(0);
        if (!item.isEmpty()) return CraftItemStack.asCraftMirror(item);
        return null;
    }

    @Override
    public void setMatrix(ItemStack[] contents) {
        if (getMatrixInventory().getContainerSize() > contents.length)
            throw new IllegalArgumentException("Invalid inventory size; expected " + getMatrixInventory().getContainerSize() + " or less");

        for (int i = 0; i < getMatrixInventory().getContainerSize(); i++)
            getMatrixInventory().setItem(i, (i < contents.length) ? CraftItemStack.asNMSCopy(contents[i]) : net.minecraft.world.item.ItemStack.EMPTY);
    }

    @Override
    public void setResult(ItemStack item) {
        List<net.minecraft.world.item.ItemStack> contents = ((ContainerBridge)getResultInventory()).getContents();
        contents.set(0, CraftItemStack.asNMSCopy(item));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Recipe getRecipe() {
        net.minecraft.world.item.crafting.Recipe recipe = ((ContainerBridge)getInventory()).getCurrentRecipe();
        return recipe == null ? null : ((RecipeHolderBridge)recipe).toBukkitRecipe();
    }
}

package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Preconditions;
import net.minecraft.world.Container;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SaddledMountInventory;

public class CraftInventorySaddledMount extends CraftInventory implements SaddledMountInventory {
    // Cardboard: From AbstractMountInventoryMenu
    public static final int SLOT_SADDLE = 0; // Paper - fix missing static
    public static final int SLOT_BODY_ARMOR = 1; // Paper - fix missing static
    public static final int SLOT_INVENTORY_START = 2; // Paper - fix missing static
    
    private final Container bodyArmorInventory;
    private final Container saddleInventory;

    public CraftInventorySaddledMount(Container inventory, final Container bodyArmorInventory, final Container saddleInventory) {
        super(inventory);
        this.bodyArmorInventory = bodyArmorInventory;
        this.saddleInventory = saddleInventory;
    }

    @Override
    public ItemStack getSaddle() {
        return this.getItem(SLOT_SADDLE);
    }

    @Override
    public void setSaddle(ItemStack stack) {
        this.setItem(SLOT_SADDLE, stack);
    }

    public Container getMainInventory() {
        return this.inventory;
    }

    public Container getArmorInventory() {
        return this.bodyArmorInventory;
    }

    public Container getSaddleInventory() {
        return this.saddleInventory;
    }

    public ItemStack getArmor() {
        return this.getItem(SLOT_BODY_ARMOR);
    }

    public void setArmor(ItemStack armor) {
        this.setItem(SLOT_BODY_ARMOR, armor);
    }

    @Override
    public int getSize() {
        return this.getMainInventory().getContainerSize() + this.getArmorInventory().getContainerSize() + this.getSaddleInventory().getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return this.getMainInventory().isEmpty() && this.getArmorInventory().isEmpty() && this.getSaddleInventory().isEmpty();
    }

    @Override
    public ItemStack[] getContents() {
        ItemStack[] items = new ItemStack[this.getSize()];

        items[SLOT_SADDLE] = this.getSaddle();
        items[SLOT_BODY_ARMOR] = this.getArmor();

        for (int i = SLOT_INVENTORY_START; i < items.length; i++) {
            net.minecraft.world.item.ItemStack item = this.getMainInventory().getItem(i - SLOT_INVENTORY_START);
            items[i] = item.isEmpty() ? null : CraftItemStack.asCraftMirror(item);
        }

        return items;
    }

    @Override
    public void setContents(ItemStack[] items) {
        Preconditions.checkArgument(items.length <= this.getSize(), "Invalid inventory size (%s); expected %s or less", items.length, this.getSize());

        this.setSaddle(ArrayUtils.get(items, SLOT_SADDLE));
        this.setArmor(ArrayUtils.get(items, SLOT_BODY_ARMOR));

        for (int i = SLOT_INVENTORY_START; i < this.getSize(); i++) {
            net.minecraft.world.item.ItemStack item = i >= items.length ? net.minecraft.world.item.ItemStack.EMPTY : CraftItemStack.asNMSCopy(items[i]);
            this.getMainInventory().setItem(i - SLOT_INVENTORY_START, item);
        }
    }

    @Override
    public ItemStack getItem(final int index) {
        if (index == SLOT_SADDLE) {
            final net.minecraft.world.item.ItemStack item = this.getSaddleInventory().getItem(0);
            return item.isEmpty() ? null : CraftItemStack.asCraftMirror(item);
        } else if (index == SLOT_BODY_ARMOR) {
            final net.minecraft.world.item.ItemStack item = this.getArmorInventory().getItem(0);
            return item.isEmpty() ? null : CraftItemStack.asCraftMirror(item);
        } else {
            int shiftedIndex = index;
            if (index > SLOT_SADDLE) {
                shiftedIndex--;
            }
            if (index > SLOT_BODY_ARMOR) {
                shiftedIndex--;
            }

            final net.minecraft.world.item.ItemStack item = this.getMainInventory().getItem(shiftedIndex);
            return item.isEmpty() ? null : CraftItemStack.asCraftMirror(item);
        }
    }

    @Override
    public void setItem(final int index, final ItemStack item) {
        if (index == SLOT_SADDLE) {
            this.getSaddleInventory().setItem(0, CraftItemStack.asNMSCopy(item));
        } else if (index == SLOT_BODY_ARMOR) {
            this.getArmorInventory().setItem(0, CraftItemStack.asNMSCopy(item));
        } else {
            int shiftedIndex = index;
            if (index > SLOT_SADDLE) {
                shiftedIndex--;
            }
            if (index > SLOT_BODY_ARMOR) {
                shiftedIndex--;
            }
            this.getMainInventory().setItem(shiftedIndex, CraftItemStack.asNMSCopy(item));
        }
    }
}

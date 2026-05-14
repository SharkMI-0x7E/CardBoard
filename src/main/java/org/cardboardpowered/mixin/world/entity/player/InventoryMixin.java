package org.cardboardpowered.mixin.world.entity.player;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.bukkit.inventory.InventoryHolder;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import org.cardboardpowered.bridge.world.ContainerBridge;
import org.cardboardpowered.bridge.world.entity.player.InventoryBridge;

@Mixin(Inventory.class)
public abstract class InventoryMixin implements Container, ContainerBridge, InventoryBridge {
    @Shadow
    @Final
    public Player player;
    @Shadow
    @Final
    public EntityEquipment equipment;
    @Shadow
    @Final
    public static Int2ObjectMap<EquipmentSlot> EQUIPMENT_SLOT_MAPPING;
    @Shadow
    @Final
    public NonNullList<ItemStack> items;

    @Shadow
    public abstract boolean hasRemainingSpaceForItem(ItemStack itemStack, ItemStack itemStack2);

    // Paper start - add fields and methods
    @Unique
    private static final EquipmentSlot[] EQUIPMENT_SLOTS_SORTED_BY_INDEX = EQUIPMENT_SLOT_MAPPING.int2ObjectEntrySet()
            .stream()
            .sorted(java.util.Comparator.comparingInt(it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry::getIntKey))
            .map(java.util.Map.Entry::getValue).toArray(EquipmentSlot[]::new);
    @Unique
    public java.util.List<org.bukkit.entity.HumanEntity> transaction = new java.util.ArrayList<>();
    @Unique
    private int maxStack = MAX_STACK;

    @Override
    public java.util.List<ItemStack> getContents() {
        java.util.List<ItemStack> combined = new java.util.ArrayList<>(this.items.size() + EQUIPMENT_SLOT_MAPPING.size());
        combined.addAll(this.items);
        for (EquipmentSlot equipmentSlot : EQUIPMENT_SLOTS_SORTED_BY_INDEX) {
            ItemStack itemStack = this.equipment.get(equipmentSlot);
            combined.add(itemStack); // Include empty items
        };
        return combined;
    }

    public java.util.List<ItemStack> getArmorContents() {
        java.util.List<ItemStack> items = new java.util.ArrayList<>(4);
        for (EquipmentSlot equipmentSlot : EQUIPMENT_SLOTS_SORTED_BY_INDEX) {
            if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                items.add(this.equipment.get(equipmentSlot));
            }
        }
        return items;
    }

    public java.util.List<ItemStack> getExtraContent() {
        java.util.List<ItemStack> items = new java.util.ArrayList<>();
        for (EquipmentSlot equipmentSlot : EQUIPMENT_SLOTS_SORTED_BY_INDEX) {
            if (equipmentSlot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) { // Non humanoid armor is considered extra
                items.add(this.equipment.get(equipmentSlot));
            }
        }
        return items;
    }

    @Override
    public void onOpen(org.bukkit.craftbukkit.entity.CraftHumanEntity player) {
        this.transaction.add(player);
    }

    @Override
    public void onClose(org.bukkit.craftbukkit.entity.CraftHumanEntity player) {
        this.transaction.remove(player);
    }

    @Override
    public java.util.List<org.bukkit.entity.HumanEntity> getViewers() {
        return this.transaction;
    }

    @Override
    public org.bukkit.inventory.InventoryHolder getOwner() {
        return (InventoryHolder) ((EntityBridge)this.player).getBukkitEntity();
    }

    @Override
    public int getMaxStackSize() {
        return this.maxStack;
    }

    @Override
    public void cardboard$setMaxStackSize(int size) {
        this.maxStack = size;
    }

    @Override
    public org.bukkit.Location getLocation() {
        return ((EntityBridge)this.player).getBukkitEntity().getLocation();
    }
    // Paper end - add fields and methods

    // CraftBukkit start - Watch method above! :D
    @Override
    public int canHold(ItemStack itemStack) {
        int remains = itemStack.getCount();
        for (int slot = 0; slot < this.items.size(); ++slot) {
            ItemStack itemInSlot = this.getItem(slot);
            if (itemInSlot.isEmpty()) {
                return itemStack.getCount();
            }

            if (this.hasRemainingSpaceForItem(itemInSlot, itemStack)) {
                remains -= (itemInSlot.getMaxStackSize() < this.getMaxStackSize() ? itemInSlot.getMaxStackSize() : this.getMaxStackSize()) - itemInSlot.getCount();
            }
            if (remains <= 0) {
                return itemStack.getCount();
            }
        }

        ItemStack itemInOffhand = this.equipment.get(EquipmentSlot.OFFHAND);
        if (this.hasRemainingSpaceForItem(itemInOffhand, itemStack)) {
            remains -= (itemInOffhand.getMaxStackSize() < this.getMaxStackSize() ? itemInOffhand.getMaxStackSize() : this.getMaxStackSize()) - itemInOffhand.getCount();
        }
        if (remains <= 0) {
            return itemStack.getCount();
        }

        return itemStack.getCount() - remains;
    }
    // CraftBukkit end
}
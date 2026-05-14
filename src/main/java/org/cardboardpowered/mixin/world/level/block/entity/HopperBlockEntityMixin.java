package org.cardboardpowered.mixin.world.level.block.entity;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.world.ContainerBridge;
import org.cardboardpowered.bridge.world.level.LevelBridge;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin implements Container, ContainerBridge {

    @Shadow
    public NonNullList<ItemStack> items;

    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    private int maxStack = MAX_STACK;

    public List<ItemStack> getContents() {
        return this.items;
    }

    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
    }

    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
    }

    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public int getMaxStackSize() {
        return maxStack;
    }

    public void cardboard$setMaxStackSize(int size) {
        maxStack = size;
    }

    @Override 
    public InventoryHolder getOwner() {
        HopperBlockEntity b = (HopperBlockEntity) (Object)this;
        if (b.level == null) return null;
        org.bukkit.block.Block block = ((LevelBridge)b.level).cardboard$getWorld().getBlockAt(b.worldPosition.getX(), b.worldPosition.getY(), b.worldPosition.getZ());
        if (block == null) {
            org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.WARNING, "No block for owner at %s %d %d %d", new Object[]{b.level, b.worldPosition.getX(), b.worldPosition.getY(), b.worldPosition.getZ()});
            return null;
        }
        org.bukkit.block.BlockState state = block.getState();
        return (state instanceof InventoryHolder) ? (InventoryHolder) state : null;
    }

    @Override
    public Location getLocation() {
        HopperBlockEntity b = (HopperBlockEntity) (Object)this;
        return new Location(((LevelBridge)b.level).cardboard$getWorld(), b.worldPosition.getX(), b.worldPosition.getY(), b.worldPosition.getZ());
    }

    @Inject(at = @At("HEAD"), method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/item/ItemEntity;)Z", cancellable = true)
    private static void extract1(net.minecraft.world.Container iinventory, ItemEntity entityitem, CallbackInfoReturnable<Boolean> ci) {
        try {
            if (iinventory instanceof ContainerBridge) {
                InventoryPickupItemEvent event = new InventoryPickupItemEvent(((ContainerBridge)iinventory).getOwner().getInventory(),(org.bukkit.entity.Item) ((EntityBridge)entityitem).getBukkitEntity());
                Bukkit.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled())
                    ci.setReturnValue(false);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Inject(at = @At("HEAD"), method = "tryTakeInItemFromSlot(Lnet/minecraft/world/level/block/entity/Hopper;Lnet/minecraft/world/Container;ILnet/minecraft/core/Direction;)Z", cancellable = true)
    private static void extract2(Hopper ihopper, net.minecraft.world.Container iinventory, int i, Direction enumdirection, CallbackInfoReturnable<Boolean> ci) {
        ItemStack itemstack = iinventory.getItem(i);
        boolean error = false;

        try {
            if (!itemstack.isEmpty() && canTakeItemFromContainer(ihopper, iinventory, itemstack, i, enumdirection)) {
                ItemStack itemstack1 = itemstack.copy();
                if (iinventory instanceof ContainerBridge && ihopper instanceof ContainerBridge) {
                    CraftItemStack oitemstack = CraftItemStack.asCraftMirror(iinventory.removeItem(i, 1));
                    org.bukkit.inventory.Inventory sourceInventory;
                    if (iinventory instanceof CompoundContainer) {
                        sourceInventory = new CraftInventoryDoubleChest((CompoundContainer) iinventory);
                    } else {
                       // sourceInventory = ((IMixinInventory)iinventory).getOwner().getInventory();
                    }
                    
                    sourceInventory = new CraftInventory(iinventory);


                    InventoryMoveItemEvent event = new InventoryMoveItemEvent(sourceInventory, oitemstack.clone(), ((ContainerBridge)ihopper).getOwner().getInventory(), false);
                    Bukkit.getServer().getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        iinventory.setItem(i, itemstack1);
                        // TODO: Somehow this breaks Mixin?
                       // if (ihopper instanceof HopperBlockEntity) {
                        //  ((HopperBlockEntity) ihopper).setCooldown(8); // Delay hopper checks
                       // } //else if (ihopper instanceof HopperMinecartEntity) {
                         //   ((HopperMinecartEntity) ihopper).setTransferCooldown(4); // Delay hopper minecart checks
                        //}
                        ci.setReturnValue(false);
                        return;
                    }
                  //  int origCount = event.getItem().getAmount();
                    ItemStack itemstack2 = addItem(iinventory, ihopper, CraftItemStack.asNMSCopy(event.getItem()), null);
                    if (itemstack2.isEmpty()) {
                        iinventory.setChanged();
                        ci.setReturnValue(true);
                        return;
                    }
                   // itemstack1.decrement(origCount - itemstack2.getCount());
                    iinventory.setItem(i, itemstack1);
                } else {
                    error = true;
                }
            }
            if (!error) ci.setReturnValue(false);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Shadow
    public static ItemStack addItem(net.minecraft.world.Container iinventory, net.minecraft.world.Container iinventory1, ItemStack itemstack, Direction enumdirection) {
        return null;
    }

    @Shadow
    public static boolean canTakeItemFromContainer(Container hopperInventory, Container fromInventory, ItemStack stack, int slot, Direction facing) {
    	return false;
    }

    //@Shadow
    //public static boolean canExtract(net.minecraft.inventory.Inventory iinventory, ItemStack itemstack, int i, Direction enumdirection) {
    //    return false;
    //}

}
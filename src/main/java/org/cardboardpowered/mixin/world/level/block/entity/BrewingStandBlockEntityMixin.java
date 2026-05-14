package org.cardboardpowered.mixin.world.level.block.entity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.cardboardpowered.bridge.world.level.block.entity.BrewingStandBlockEntityBridge;
import org.cardboardpowered.bridge.world.ContainerBridge;
import org.cardboardpowered.bridge.world.level.LevelBridge;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin implements Container, ContainerBridge, BrewingStandBlockEntityBridge {

    @Shadow
    public int fuel;

    @Shadow
    public NonNullList<ItemStack> items;

    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    private int maxStack = 64;

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
    public List<ItemStack> getContents() {
        return this.items;
    }

    @Override
    public int getMaxStackSize() {
        return maxStack;
    }

    @Override
    public void cardboard$setMaxStackSize(int size) {
        maxStack = size;
    }

    /**
     * @author CardboardPowered.org
     * @reason BrewingStandFuelEvent
     */
    @Inject(at = @At("HEAD"), method = "serverTick", cancellable = true)
    private static void doBukkitEvent_BrewingStandFuelEvent(Level world, BlockPos pos, BlockState state, BrewingStandBlockEntity be, CallbackInfo ci) {
        ItemStack itemstack = (ItemStack) ((BrewingStandBlockEntityBridge)be).cardboard_getInventory().get(4);

        if (be.fuel <= 0 && itemstack.getItem() == Items.BLAZE_POWDER) {
            BrewingStandFuelEvent event = new BrewingStandFuelEvent(((LevelBridge)be.level).cardboard$getWorld().getBlockAt(be.worldPosition.getX(), be.worldPosition.getY(), be.worldPosition.getZ()), CraftItemStack.asCraftMirror(itemstack), 20);
            CraftServer.INSTANCE.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                ci.cancel();
                return;
            }

            be.fuel = event.getFuelPower();
            if (be.fuel > 0 && event.isConsuming()) itemstack.shrink(1);
        }
    }

    // TODO 1.17ify:
   /* @Inject(at = @At("HEAD"), method = "craft", cancellable = true)
    public void doBukkitEvent_BrewEvent(CallbackInfo ci) {
        InventoryHolder owner = this.getOwner();
        if (owner != null) {
            BlockPos pos = ((BrewingStandBlockEntity)(Object)this).getPos();
            BrewEvent event = new BrewEvent(((IMixinWorld)((BrewingStandBlockEntity)(Object)this).getWorld()).getCraftWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()), (org.bukkit.inventory.BrewerInventory) owner.getInventory(), this.fuel);
            org.bukkit.Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                ci.cancel();
                return;
            }
        }
    }*/

    @Override public InventoryHolder getOwner() {return null;}
    @Override public Location getLocation() {return null;}

    @Override
    public NonNullList<ItemStack> cardboard_getInventory() {
        return items;
    }

}
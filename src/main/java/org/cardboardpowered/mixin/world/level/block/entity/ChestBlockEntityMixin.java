package org.cardboardpowered.mixin.world.level.block.entity;

import java.util.List;

import net.minecraft.world.Container;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.cardboardpowered.bridge.world.ContainerBridge;
import org.cardboardpowered.bridge.world.level.LevelBridge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;

/**
 * @see {@link me.isaiah.common.cmixin.IMixinChestBlockEntity}
 */
@Mixin(ChestBlockEntity.class)
public abstract class ChestBlockEntityMixin implements Container, ContainerBridge {
	
    @Shadow
    private ContainerOpenersCounter openersCounter;

    @Shadow public NonNullList<ItemStack> items;

    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    private int maxStack = MAX_STACK;

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
    public void cardboard$setMaxStackSize(int size) {
        maxStack = size;
    }

    @Override
    public int getMaxStackSize() {
        return maxStack;
    }

    @Override
    public InventoryHolder getOwner() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Location getLocation() {
        BlockPos pos = ((ChestBlockEntity)(Object)this).worldPosition;
        return new Location(((LevelBridge)(((ChestBlockEntity)(Object)this).level)).cardboard$getWorld(), pos.x, pos.y, pos.z);
    }

    private int oldPower_B;

    /**
     * @reason Redstone Event - store old power value
     */
    @Inject(at = @At("HEAD"), method = "startOpen")
    public void doBukkitEvent_RedstoneChange_1(ContainerUser e, CallbackInfo ci) {
        oldPower_B = Math.max(0, Math.min(15, openersCounter.getOpenerCount())); // CraftBukkit - Get power before new viewer is added
    }

    /**
     * @reason Redstone Event
     */
    @Inject(at = @At("TAIL"), method = "startOpen")
    public void doBukkitEvent_RedstoneChange_2(ContainerUser e, CallbackInfo ci) {
        if (((ChestBlockEntity)(Object)this).getBlockState().getBlock() == Blocks.TRAPPED_CHEST) {
            int newPower = Math.max(0, Math.min(15, openersCounter.getOpenerCount()));
            if (oldPower_B != newPower)
                CraftEventFactory.callRedstoneChange(((ChestBlockEntity)(Object)this).level, ((ChestBlockEntity)(Object)this).worldPosition, oldPower_B, newPower);
        }
    }

}
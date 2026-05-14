package org.cardboardpowered.mixin.world.inventory;

import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import org.bukkit.Location;
import org.bukkit.inventory.InventoryHolder;
import org.cardboardpowered.mixin.world.SimpleContainerMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import org.cardboardpowered.bridge.world.level.LevelBridge;

@Mixin(PlayerEnderChestContainer.class)
public abstract class PlayerEnderChestContainerMixin extends SimpleContainerMixin {

    @Shadow private EnderChestBlockEntity activeChest;

    public InventoryHolder getBukkitOwner() {
        return null; // TODO
    }

    @Override
    public Location getLocation() {
        return new Location(((LevelBridge)this.activeChest.getLevel()).cardboard$getWorld(), this.activeChest.getBlockPos().getX(), this.activeChest.getBlockPos().getY(), this.activeChest.getBlockPos().getZ());
    }

}
package org.cardboardpowered.mixin.world.inventory;

import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import net.minecraft.world.Container;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.HorseInventoryMenu;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.world.ContainerBridge;

@Mixin(HorseInventoryMenu.class)
public class HorseInventoryMenuMixin extends AbstractContainerMenuMixin {

    // @Shadow
    // public Inventory inventory;

    private CraftInventoryView bukkitEntity = null;
    private Inventory playerInv;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/animal/equine/AbstractHorse;I)V", at = @At("TAIL"))
    public void setPlayerInv(int i, Inventory playerinventory, Container iinventory, final AbstractHorse  entityhorseabstract, int slot_col_count, CallbackInfo ci) {
        this.playerInv = playerinventory;
    }

    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null)
            return bukkitEntity;
        return bukkitEntity = new CraftInventoryView((Player)((EntityBridge)this.playerInv.player).getBukkitEntity(), ((ContainerBridge)playerInv).getOwner().getInventory(), (HorseInventoryMenu)(Object)this);
    }


}
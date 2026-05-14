package org.cardboardpowered.mixin.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.inventory.CraftInventoryLectern;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.cardboardpowered.bridge.world.entity.EntityBridge;

@Mixin(LecternMenu.class)
public class LecternMenuMixin extends AbstractContainerMenuMixin {

    @Shadow
    public Container lectern;

    @Shadow
    public ContainerData lecternData;

    private CraftInventoryView bukkitEntity = null;
    private org.bukkit.entity.Player player;

    @Inject(method = "<init>(ILnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;)V", at = @At("TAIL"))
    public void setPlayerInv(int i, Container iinventory, ContainerData icontainerproperties, CallbackInfo ci) {
        this.player = (org.bukkit.entity.Player)((EntityBridge)((Inventory)iinventory).player).getBukkitEntity();
    }

    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null) return bukkitEntity;

        CraftInventoryLectern inventory = new CraftInventoryLectern(this.lectern);
        bukkitEntity = new CraftInventoryView(this.player, inventory, (LecternMenu)(Object)this);
        return bukkitEntity;
    }

    @Inject(method = "clickMenuButton", at = @At("HEAD"), cancellable = true)
    public void cardboard$onClickMenuButton(Player entityhuman, int i, CallbackInfoReturnable<Boolean> cir) {
        if (i != 3) return;

        if (!entityhuman.mayBuild()) {
            cir.setReturnValue(false);
            cir.cancel();
        }

        PlayerTakeLecternBookEvent event = new PlayerTakeLecternBookEvent(player, ((CraftInventoryLectern) getBukkitView().getTopInventory()).getHolder());
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }

        ItemStack itemstack = this.lectern.removeItemNoUpdate(0);
        this.lectern.setChanged();
        if (!entityhuman.getInventory().add(itemstack)) {
            entityhuman.drop(itemstack, false);
        }

        cir.setReturnValue(true);
        cir.cancel();
    }

    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    public void stillValidCraftBukkit(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!this.checkReachable) cir.setReturnValue(true); // CraftBukkit
    }
}
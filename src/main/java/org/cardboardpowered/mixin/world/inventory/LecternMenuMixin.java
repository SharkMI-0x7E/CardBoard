/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
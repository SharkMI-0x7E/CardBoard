/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors
 *
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

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import org.bukkit.craftbukkit.inventory.CraftInventoryCrafting;
import org.bukkit.entity.Player;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;

@Mixin(InventoryMenu.class)
public class InventoryMenuMixin extends AbstractContainerMenuMixin implements MenuProvider {

	// @Shadow public RecipeInputInventory craftingInput;
    // @Shadow private CraftingResultInventory craftingResult;
    private CraftInventoryView bukkitEntity = null;
    private Inventory player;

    @Inject(method = "<init>", at = @At("TAIL"))
    
    
    public void setPlayerInv(Inventory playerinventory, boolean flag, net.minecraft.world.entity.player.Player entityhuman, CallbackInfo ci) {
       // this.craftingResult = new CraftingResultInventory();
       // this.craftingInput = new CraftingInventory((PlayerScreenHandler)(Object)this, 2, 2);
        this.player = playerinventory;
        
        // TODO: 1.19:
        // setTitle(new TranslatableText("container.crafting"));
    }

    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null)
            return bukkitEntity;
        
        InventoryMenu thiz = (InventoryMenu)(Object)this;

        CraftInventoryCrafting inventory = new CraftInventoryCrafting(thiz.craftSlots, thiz.resultSlots);
        bukkitEntity = new CraftInventoryView((Player)((ServerPlayerBridge)this.player.player).getBukkitEntity(), inventory, (InventoryMenu)(Object)this);
        return bukkitEntity;
    }

    @Override
    public AbstractContainerMenu createMenu(int arg0, Inventory arg1, net.minecraft.world.entity.player.Player arg2) {
        return new InventoryMenu(arg1, true, arg2);
    }

    @Override
    public Component getDisplayName() {
        return this.getTitle();
    }

}
/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2024 CardboardPowered.org and contributors
 * Copyright (C) 2025-2026 SharkMI and contributors
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

import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.bukkit.craftbukkit.inventory.CraftInventoryFurnace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceMenu.class)
public class AbstractFurnaceMenuMixin extends AbstractContainerMenuMixin {

    @Shadow
    public Container container;

    private CraftInventoryView bukkitEntity = null;
    private Inventory playerInv;

    // Caused by: org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException: Invalid descriptor on bukkitfabric.mixins.json:screen.MixinAbstractFurnaceScreenHandler from mod cardboard->@Inject:
    // setPlayerInv(Lnet/minecraft/screen/ScreenHandlerType;Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/recipe/book/RecipeBookCategory;ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/screen/PropertyDelegate;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V!
    // Expected    (Lnet/minecraft/screen/ScreenHandlerType;Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/recipe/book/RecipeBookType;ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/screen/PropertyDelegate;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V
    // but found   (Lnet/minecraft/screen/ScreenHandlerType;Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/recipe/book/RecipeBookCategory;ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/screen/PropertyDelegate;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V [INJECT_APPLY Applicator Phase -> bukkitfabric.mixins.json:screen.MixinAbstractFurnaceScreenHandler from mod cardboard -> Apply Injections ->  -> Inject -> bukkitfabric.mixins.json:screen.MixinAbstractFurnaceScreenHandler from mod cardboard->@Inject::setPlayerInv(Lnet/minecraft/screen/ScreenHandlerType;Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/recipe/book/RecipeBookCategory;ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/screen/PropertyDelegate;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V]

   /*
    * "Lnet/minecraft/screen/AbstractFurnaceScreenHandler;<init>(
    * Lnet/minecraft/screen/ScreenHandlerType;
    * Lnet/minecraft/recipe/RecipeType;
    * Lnet/minecraft/registry/RegistryKey;
    * Lnet/minecraft/recipe/book/RecipeBookType;
    * I
    * Lnet/minecraft/entity/player/PlayerInventory;
    * Lnet/minecraft/inventory/Inventory;
    * Lnet/minecraft/screen/PropertyDelegate;
    * )V",
    */

    
    @Inject(
    	method =
    		   "<init>(Lnet/minecraft/world/inventory/MenuType;Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/inventory/RecipeBookType;ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;)V",
    		// "Lnet/minecraft/screen/AbstractFurnaceScreenHandler;<init>(Lnet/minecraft/screen/ScreenHandlerType;Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/recipe/book/RecipeBookType;ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/screen/PropertyDelegate;)V",
    		// "<init>(Lnet/minecraft/screen/ScreenHandlerType;Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/recipe/book/RecipeBookCategory;ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/screen/PropertyDelegate;)V",
    	at = @At("TAIL")
    )
    public void setPlayerInv(
    		MenuType<?> sh,
    		RecipeType<? extends AbstractCookingRecipe> recipes,
    		ResourceKey key,
    		RecipeBookType type,
    		int i,
    		Inventory playerinventory,
    		Container inv,
    		ContainerData prop,
    		CallbackInfo ci
    	) {
        this.playerInv = playerinventory;
    }

    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null) return bukkitEntity;

        CraftInventoryFurnace inventory = new CraftInventoryFurnace((AbstractFurnaceBlockEntity) this.container);
        bukkitEntity = new CraftInventoryView((org.bukkit.entity.Player)((EntityBridge)this.playerInv.player).getBukkitEntity(), inventory, (AbstractFurnaceMenu)(Object)this);
        return bukkitEntity;
    }

    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    public void stillValidCraftBukkit(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!this.checkReachable) cir.setReturnValue(true); // CraftBukkit
    }
}
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
package org.cardboardpowered.mixin.world.entity.decoration;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.cardboardpowered.mixin.world.entity.EntityMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.cardboardpowered.bridge.world.entity.decoration.ArmorStandBridge;
import org.cardboardpowered.bridge.world.entity.EntityBridge;

@Mixin(net.minecraft.world.entity.decoration.ArmorStand.class)
public class ArmorStandMixin extends EntityMixin implements ArmorStandBridge {

    public boolean canMove = true; // Paper

    @Override
    public void setHideBasePlateBF(boolean b) {
        setNoBasePlate(b);
    }

    @Override
    public void setShowArmsBF(boolean arms) {
        setShowArms(arms);
    }

    @Override
    public void setSmallBF(boolean small) {
        setSmall(small);
    }

    @Override
    public void setMarkerBF(boolean marker) {
        setMarker(marker);
    }

    @Override
    public boolean canMoveBF() {
        return canMove;
    }

    @Override
    public void setCanMoveBF(boolean b) {
        this.canMove = b;
    }

    @Shadow public void setNoBasePlate(boolean flag) {}
    @Shadow public void setMarker(boolean flag) {}
    @Shadow public void setShowArms(boolean flag) {}
    @Shadow public void setSmall(boolean flag) {}

    // Paper start
    @Override
    public void move(MoverType moveType, Vec3 vec3d) {
        if (this.canMove) super.move(moveType, vec3d);
    }
    // Paper end


    @Inject(
    		method = "swapItem", cancellable = true,
    		at = @At(
    				value = "INVOKE",
    				target = "Lnet/minecraft/world/entity/player/Player;hasInfiniteMaterials()Z"
    			)
    	)
    public void cardboard$armorstand_PlayerArmorStandManipulateEvent(net.minecraft.world.entity.player.Player playerEntity, net.minecraft.world.entity.EquipmentSlot slotType, ItemStack itemStack, InteractionHand hand, CallbackInfoReturnable<Boolean> cir) {
    	ItemStack itemStack1 = ((net.minecraft.world.entity.decoration.ArmorStand)(Object)this).getItemBySlot(slotType);

        org.bukkit.inventory.ItemStack armorStandItem = CraftItemStack.asCraftMirror(itemStack1);
        org.bukkit.inventory.ItemStack playerHeldItem = CraftItemStack.asCraftMirror(itemStack);

        Player player = (Player) ((EntityBridge) playerEntity).getBukkitEntity();
        ArmorStand self = (ArmorStand) ((EntityBridge) this).getBukkitEntity();

        EquipmentSlot slot = CraftEquipmentSlot.getSlot(slotType);
        
        EquipmentSlot bukkitHand = EquipmentSlot.HAND;
        if (hand == InteractionHand.OFF_HAND) {
        	bukkitHand = EquipmentSlot.OFF_HAND;
        }

        PlayerArmorStandManipulateEvent event = new PlayerArmorStandManipulateEvent(player, self, playerHeldItem, armorStandItem, slot, bukkitHand);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            cir.setReturnValue(true);
        }
    }
    
    /**
     * PlayerArmorStandManipulateEvent
     * 
     * @author Arclight
     * @author Cardboard
     */
    /*
    @Inject(method = "equip", cancellable = true,at = @At(value = "INVOKE", target =
            "Lnet/minecraft/entity/player/PlayerEntity;getAbilities()Lnet/minecraft/entity/player/PlayerAbilities;"))
    public void doBukkitEvent_PlayerArmorStandManipulateEvent(PlayerEntity playerEntity, net.minecraft.entity.EquipmentSlot slotType, ItemStack itemStack,
            Hand hand, CallbackInfoReturnable<Boolean> cir) {
        ItemStack itemStack1 = ((ArmorStandEntity)(Object)this).getEquippedStack(slotType);

        org.bukkit.inventory.ItemStack armorStandItem = CraftItemStack.asCraftMirror(itemStack1);
        org.bukkit.inventory.ItemStack playerHeldItem = CraftItemStack.asCraftMirror(itemStack);

        Player player = (Player) ((IMixinEntity) playerEntity).getBukkitEntity();
        ArmorStand self = (ArmorStand) ((IMixinEntity) this).getBukkitEntity();

        EquipmentSlot slot = com.javazilla.bukkitfabric.Utils.getSlot(slotType);
        PlayerArmorStandManipulateEvent event = new PlayerArmorStandManipulateEvent(player, self, playerHeldItem, armorStandItem, slot);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            cir.setReturnValue(true);
        }
    }
    */

    // CraftBukkit start - SPIGOT-3607, SPIGOT-3637
    @Override
    public float cardboard$getBukkitYaw() {
        return this.getYRot();
    }
    // CraftBukkit end
}
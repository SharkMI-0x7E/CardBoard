/**
 * Cardboard - Paper API for Fabric
 * Copyright (C) 2020-2025 Cardboard Contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 */
package org.cardboardpowered.mixin.world.entity.monster.piglin;

import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.bukkit.craftbukkit.event.CraftEventFactory;

@MixinInfo(events = {"EntityPickupItemEvent"})
@Mixin(value = PiglinAi.class, priority = 900)
public class PiglinAiMixin {

    /**
     * @reason .
     * @author Cardboard
     */
    @Overwrite
    public static void pickUpItem(ServerLevel world, Piglin entitypiglin, ItemEntity entityitem) {
        stopWalking(entitypiglin);
        ItemStack itemstack;

        if (entityitem.getItem().getItem() == Items.GOLD_NUGGET && !CraftEventFactory.callEntityPickupItemEvent(entitypiglin, entityitem, 0, false).isCancelled()) {
            entitypiglin.take(entityitem, entityitem.getItem().getCount());
            itemstack = entityitem.getItem();
            entityitem.remove(RemovalReason.DISCARDED);
        } else if (!CraftEventFactory.callEntityPickupItemEvent(entitypiglin, entityitem, entityitem.getItem().getCount() - 1, false).isCancelled()) {
            entitypiglin.take(entityitem, 1);
            itemstack = removeOneItemFromItemEntity(entityitem);
        } else return;

        if (isLovedItem(itemstack)) {
            entitypiglin.getBrain().eraseMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
            holdInOffhand(world, entitypiglin, itemstack);
            admireGoldItem((LivingEntity) entitypiglin);
        } else if (isFood(itemstack) && !hasEatenRecently(entitypiglin)) {
            eat(entitypiglin);
        } else {
            boolean flag = !entitypiglin.equipItemIfPossible(world, itemstack).equals(ItemStack.EMPTY);
            if (!flag) putInInventory(entitypiglin, itemstack);
        }
    }

    // This class likes static methods
    @Shadow public static boolean isLovedItem(ItemStack item) {return false;}
    @Shadow public static void eat(Piglin entitypiglin) {}
    @Shadow public static void admireGoldItem(LivingEntity entityliving) {}
    @Shadow public static boolean hasEatenRecently(Piglin entitypiglin) {return false;}
    @Shadow public static boolean isFood(ItemStack item) {return false;}
    @Shadow public static ItemStack removeOneItemFromItemEntity(ItemEntity entityitem) {return null;}
    @Shadow public static void holdInOffhand(ServerLevel world, Piglin entitypiglin, ItemStack itemstack) {}
    @Shadow public static void stopWalking(Piglin entitypiglin) {}
    @Shadow public static void putInInventory(Piglin entitypiglin, ItemStack itemstack) {}

}
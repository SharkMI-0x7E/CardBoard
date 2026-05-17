/**
 * Copyright (C) 2026 SharkMI and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 */
package org.cardboardpowered.mixin.world.entity.player;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.cardboardpowered.bridge.world.entity.player.PlayerBridge;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.world.entity.LivingEntityBridge;
import org.cardboardpowered.mixin.world.entity.LivingEntityMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntityMixin implements EntityBridge, LivingEntityBridge, PlayerBridge {
    @Shadow
    public abstract Inventory getInventory();

    @Shadow
    public AbstractContainerMenu containerMenu;

    @Override
    public org.bukkit.craftbukkit.entity.CraftHumanEntity getBukkitEntity() {
        return (org.bukkit.craftbukkit.entity.CraftHumanEntity) super.getBukkitEntity();
    }
}

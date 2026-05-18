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
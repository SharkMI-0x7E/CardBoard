/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2024 CardboardPowered.org and contributors
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
package org.cardboardpowered.mixin.world.level.block;

import org.cardboardpowered.extras.BukkitChestDoubleInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

@Mixin(targets = "net.minecraft.world.level.block.ChestBlock$2")
public class ChestBlock_2Mixin {
    @Inject(method = "acceptDouble(Lnet/minecraft/world/level/block/entity/ChestBlockEntity;Lnet/minecraft/world/level/block/entity/ChestBlockEntity;)Ljava/util/Optional;", at = @At("RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void bukkitCustomInventory(ChestBlockEntity chestBlockEntity, ChestBlockEntity chestBlockEntity2, CallbackInfoReturnable<Optional<MenuProvider>> cir, Container inventory) {
        cir.setReturnValue(Optional
                .of(new BukkitChestDoubleInventory(chestBlockEntity, chestBlockEntity2,
                        (net.minecraft.world.CompoundContainer) inventory)));
    }
}

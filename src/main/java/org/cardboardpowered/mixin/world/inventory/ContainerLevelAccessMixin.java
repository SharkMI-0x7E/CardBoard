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
package org.cardboardpowered.mixin.world.inventory;

import java.util.Optional;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import org.cardboardpowered.bridge.world.inventory.ContainerLevelAccessBridge;
import org.cardboardpowered.bridge.world.level.LevelBridge;

@Mixin(ContainerLevelAccess.class)
public interface ContainerLevelAccessMixin extends ContainerLevelAccessBridge {

    @Override
    default Level getWorld() {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    default BlockPos getPosition() {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    default org.bukkit.Location getLocation() {
        return new org.bukkit.Location(((LevelBridge)getWorld()).cardboard$getWorld(), getPosition().getX(), getPosition().getY(), getPosition().getZ());
    }

    /**
     * @reason Add new methods
     * @author BukkitFabric
     *
     * TODO: Cannot replace with @Inject - this @Overwrite rewrites the create() factory
     * method to return an anonymous class that implements ContainerLevelAccessBridge
     * interface with getWorld(), getPosition(), and getLocation() methods for Bukkit
     * API compatibility. The original lambda-based implementation cannot be extended
     * to add bridge interface methods.
     */
    @Overwrite
    static ContainerLevelAccess create(final Level world, final BlockPos blockposition) {
        return new ContainerLevelAccess() {

            @SuppressWarnings("unused")
            public Level getWorld() {
                return world;
            }

            @SuppressWarnings("unused")
            public BlockPos getPosition() {
                return blockposition;
            }

            @Override
            public <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> bifunction) {
                return Optional.of(bifunction.apply(world, blockposition));
            }
        };
    }

}
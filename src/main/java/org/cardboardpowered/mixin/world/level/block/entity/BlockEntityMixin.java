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
package org.cardboardpowered.mixin.world.level.block.entity;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.server.level.ServerLevel;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.cardboardpowered.bridge.world.level.block.entity.BlockEntityBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(BlockEntity.class)
public class BlockEntityMixin implements BlockEntityBridge {

    private static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = new CraftPersistentDataTypeRegistry();
    public CraftPersistentDataContainer persistentDataContainer;

    @Shadow
    private DataComponentMap components = DataComponentMap.EMPTY;
    
    @Shadow public Level level;
    @Shadow public BlockPos worldPosition;

    @Override
    public CraftPersistentDataContainer getPersistentDataContainer() {
        return persistentDataContainer;
    }

    // CraftBukkit start - add method
    @Override
    public org.bukkit.inventory.@Nullable InventoryHolder cardboard$getOwner() {
        return cardboard$getOwner(true);
    }

    @Override
    public org.bukkit.inventory.@Nullable InventoryHolder cardboard$getOwner(boolean useSnapshot) {
        if (this.level == null) return null;
        org.bukkit.block.Block block = org.bukkit.craftbukkit.block.CraftBlock.at((ServerLevel) this.level, this.worldPosition);
        org.bukkit.block.BlockState state = block.getState(useSnapshot); // Paper
        return state instanceof final org.bukkit.inventory.InventoryHolder inventoryHolder ? inventoryHolder : null;
    }
    // CraftBukkit end

    @Override
    public void setCardboardPersistentDataContainer(CraftPersistentDataContainer c) {
        this.persistentDataContainer = c;
    }

    @Override
    public CraftPersistentDataTypeRegistry getCardboardDTR() {
        return DATA_TYPE_REGISTRY;
    }
    
    @Shadow
    public void applyImplicitComponents(DataComponentGetter components) {
	}
    
    @Override
    public Set<DataComponentType<?>> applyComponentsSet(DataComponentMap defaultComponents, DataComponentPatch components) {
		final Set<DataComponentType<?>> set = new HashSet<>();
		set.add(DataComponents.BLOCK_ENTITY_DATA);
		set.add(DataComponents.BLOCK_STATE);
		final DataComponentMap componentMap = PatchedDataComponentMap.fromPatch(defaultComponents, components);
		this.applyImplicitComponents(new DataComponentGetter() {

			@Override
			public <T> T get(DataComponentType<? extends T> type) {
				set.add(type);
				return componentMap.get(type);
			}

			@Override
			public <T> T getOrDefault(DataComponentType<? extends T> type, T fallback) {
				set.add(type);
				return componentMap.getOrDefault(type, fallback);
			}
		});
		DataComponentPatch componentChanges = components.forget(set::contains);
		this.components = componentChanges.split().added();
		
		// Paper - start
		set.remove(DataComponents.BLOCK_ENTITY_DATA); // Remove as never actually added by applyImplicitComponents
		return set;
		// Paper - end
	}

}
/**
 * Cardboard
 * Copyright (C) 2020-2025
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.bridge.world.level.block.entity;

import java.util.Set;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.inventory.InventoryHolder;
import org.jspecify.annotations.Nullable;

public interface BlockEntityBridge {

    CraftPersistentDataContainer getPersistentDataContainer();

    InventoryHolder cardboard$getOwner();

    @Nullable InventoryHolder cardboard$getOwner(boolean useSnapshot);

    void setCardboardPersistentDataContainer(CraftPersistentDataContainer c);

    CraftPersistentDataTypeRegistry getCardboardDTR();

	Set<DataComponentType<?>> applyComponentsSet(DataComponentMap defaultComponents, DataComponentPatch components);

}
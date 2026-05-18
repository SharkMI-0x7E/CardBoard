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
package org.cardboardpowered.mixin.world.level.block.state;

import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.cardboardpowered.bridge.world.level.block.state.BlockStateBaseBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockStateBase.class)
public abstract class BlockStateBaseMixin implements BlockStateBaseBridge {
    @Shadow
    public abstract BlockState asState();

    // Paper start - Perf: impl cached craft block data, lazy load to fix issue with loading at the wrong time
    @Unique
    private CraftBlockData cachedCraftBlockData;
	
	@Override
    public org.bukkit.craftbukkit.block.data.CraftBlockData cardboard$createCraftBlockData() {
        if (this.cachedCraftBlockData == null) this.cachedCraftBlockData = org.bukkit.craftbukkit.block.data.CraftBlockData.createData(this.asState());
        return (org.bukkit.craftbukkit.block.data.CraftBlockData) this.cachedCraftBlockData.clone();
    }
    // Paper end - Perf: impl cached craft block data, lazy load to fix issue with loading at the wrong time
}

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
package org.cardboardpowered.mixin.world.item.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.component.TypedEntityData;
import org.cardboardpowered.bridge.world.item.component.TypedEntityDataBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TypedEntityData.class)
public class TypedEntityDataMixin<IdType> implements TypedEntityDataBridge {
    @Shadow
    @Final
    CompoundTag tag;

    @Shadow
    @Final
    IdType type;

    // Paper start - utils for item meta
    @Override
    public CompoundTag copyTagWithEntityId() {
        final CompoundTag tag = this.tag.copy();
        tag.putString("id", EntityType.getKey((EntityType<?>) this.type).toString());
        return tag;
    }

    @Override
    public CompoundTag copyTagWithBlockEntityId() {
        final CompoundTag tag = this.tag.copy();
        tag.putString("id", net.minecraft.world.level.block.entity.BlockEntityType.getKey((net.minecraft.world.level.block.entity.BlockEntityType<?>) this.type).toString());
        return tag;
    }
    // Paper end - utils for item meta
}

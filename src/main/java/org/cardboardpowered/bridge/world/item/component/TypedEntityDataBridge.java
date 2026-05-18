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
package org.cardboardpowered.bridge.world.item.component;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.component.TypedEntityData;

public interface TypedEntityDataBridge {
    public static <IdType> TypedEntityData<IdType> decode(final Codec<IdType> idTypeCodec, final CompoundTag tag) {
        return TypedEntityData.codec(idTypeCodec).decode(net.minecraft.nbt.NbtOps.INSTANCE, tag).result().orElseThrow().getFirst();
    }

    public static TypedEntityData<EntityType<?>> decodeEntity(final CompoundTag tag) {
        return decode(net.minecraft.world.entity.EntityType.CODEC, tag);
    }

    public static TypedEntityData<net.minecraft.world.level.block.entity.BlockEntityType<?>> decodeBlockEntity(final CompoundTag tag) {
        return decode(net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec(), tag);
    }

    CompoundTag copyTagWithEntityId();

    CompoundTag copyTagWithBlockEntityId();
}

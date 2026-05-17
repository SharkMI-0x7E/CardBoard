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
package org.cardboardpowered;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntityType;

// TODO: mixin this into TypedEntityData
public class TypedEntityDataExtra {

	public static <IdType> TypedEntityData<IdType> decode(Codec<IdType> idTypeCodec, CompoundTag tag) {
		return (TypedEntityData<IdType>)(TypedEntityData.codec(idTypeCodec).decode(NbtOps.INSTANCE, tag).result().orElseThrow()).getFirst();
	}

	public static TypedEntityData<EntityType<?>> decodeEntity(CompoundTag tag) {
		return decode(EntityType.CODEC, tag);
	}

	public static TypedEntityData<BlockEntityType<?>> decodeBlockEntity(CompoundTag tag) {
		return decode(BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec(), tag);
	}
	
	public static CompoundTag copyTagWithEntityId(TypedEntityData<?> data) {
		CompoundTag tag = data.copyTagWithoutId();
		tag.putString("id", EntityType.getKey((EntityType<?>)data.type()).toString());
		return tag;
	}

	public static CompoundTag copyTagWithBlockEntityId(TypedEntityData<?> data) {
		CompoundTag tag = data.copyTagWithoutId();
		tag.putString("id", BlockEntityType.getKey((BlockEntityType<?>)data.type()).toString());
		return tag;
	}
	
}

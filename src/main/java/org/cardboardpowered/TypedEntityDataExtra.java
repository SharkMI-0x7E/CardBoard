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

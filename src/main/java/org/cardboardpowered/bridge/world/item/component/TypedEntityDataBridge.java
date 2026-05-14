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

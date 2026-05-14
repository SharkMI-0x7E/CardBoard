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

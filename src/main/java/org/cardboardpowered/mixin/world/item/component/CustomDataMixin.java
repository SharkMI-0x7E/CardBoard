package org.cardboardpowered.mixin.world.item.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import org.cardboardpowered.bridge.world.item.component.CustomDataBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CustomData.class)
public class CustomDataMixin implements CustomDataBridge {
    @Shadow
    @Final
    private CompoundTag tag;

    // Paper start - expose unsafe internal compound tag for read only access
    @Deprecated
    @Override
    public CompoundTag cardboard$getUnsafe() {
        return this.tag;
    }

    @Override
    public boolean cardboard$contains(String key) {
        return this.tag.contains(key);
    }
    // Paper end - expose unsafe internal compound tag for read only access
}

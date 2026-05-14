package org.cardboardpowered.mixin.world.level;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelAccessor.class)
public interface LevelAccessorMixin {

    default ServerLevel getMinecraftWorld() {
        return (ServerLevel)(LevelAccessor)(Object)this;
    }

}
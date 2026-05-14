package org.cardboardpowered.mixin.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Mixin;

import org.cardboardpowered.bridge.world.level.block.entity.BaseContainerBlockEntityBridge;
import org.cardboardpowered.bridge.world.level.LevelBridge;

@Mixin(BaseContainerBlockEntity.class)
public class BaseContainerBlockEntityMixin implements BaseContainerBlockEntityBridge {

    @Override
    public Location getLocation() {
        BaseContainerBlockEntity lc = (BaseContainerBlockEntity)(Object)this;
        BlockPos pos = lc.getBlockPos();
        return new Location(((LevelBridge)lc.level).cardboard$getWorld(), pos.getX(), pos.getY(), pos.getZ());
    }

}
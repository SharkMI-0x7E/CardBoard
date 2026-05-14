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

package org.cardboardpowered.mixin.world.level.block.state;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.cardboardpowered.bridge.world.level.block.state.BlockStateBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockState.class)
public abstract class BlockStateMixin extends BlockBehaviour.BlockStateBase implements BlockStateBridge {
    protected BlockStateMixin(Block block, Reference2ObjectArrayMap<Property<?>, Comparable<?>> reference2ObjectArrayMap, MapCodec<BlockState> mapCodec) {
        super(block, reference2ObjectArrayMap, mapCodec);
    }

    // Paper start - optimise getType calls
    @Unique
    @javax.annotation.Nullable org.bukkit.Material cachedMaterial;

    @Override
    public final org.bukkit.Material cardboard$getBukkitMaterial() {
        if (this.cachedMaterial == null) {
            this.cachedMaterial = org.bukkit.craftbukkit.block.CraftBlockType.minecraftToBukkit(this.getBlock());
        }
        return this.cachedMaterial;
    }
    // Paper end - optimise getType calls
}

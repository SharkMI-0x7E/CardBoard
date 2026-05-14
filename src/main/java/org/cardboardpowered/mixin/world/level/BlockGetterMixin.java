package org.cardboardpowered.mixin.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockGetter.class)
public interface BlockGetterMixin<T> {

    @Shadow
    public BlockState getBlockState(BlockPos var1);

    @Shadow
    public FluidState getFluidState(BlockPos var1);

    default BlockHitResult raycastBlock(ClipContext raytrace1, BlockPos blockposition) {
        BlockState iblockdata = this.getBlockState(blockposition);
        FluidState fluid = this.getFluidState(blockposition);
        Vec3 vec3d = raytrace1.getFrom();
        Vec3 vec3d1 = raytrace1.getTo();
        VoxelShape voxelshape = raytrace1.getBlockShape(iblockdata, (BlockGetter)(Object)this, blockposition);
        BlockHitResult movingobjectpositionblock = this.clipWithInteractionOverride(vec3d, vec3d1, blockposition, voxelshape, iblockdata);
        VoxelShape voxelshape1 = raytrace1.getFluidShape(fluid, (BlockGetter)(Object)this, blockposition);
        BlockHitResult movingobjectpositionblock1 = voxelshape1.clip(vec3d, vec3d1, blockposition);
        double d0 = movingobjectpositionblock == null ? Double.MAX_VALUE : raytrace1.getFrom().distanceToSqr(movingobjectpositionblock.getLocation());
        double d1 = movingobjectpositionblock1 == null ? Double.MAX_VALUE : raytrace1.getFrom().distanceToSqr(movingobjectpositionblock1.getLocation());
        return d0 <= d1 ? movingobjectpositionblock : movingobjectpositionblock1;
    }

    @Shadow
    default public BlockHitResult clipWithInteractionOverride(Vec3 start, Vec3 end, BlockPos pos, VoxelShape shape, BlockState state) {return null;}

}

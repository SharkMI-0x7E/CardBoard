package org.cardboardpowered.bridge.world.entity.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public interface ItemFrameBridge {
    public static AABB createBoundingBoxStatic(BlockPos pos, Direction direction, boolean hasFramedMap) {
        float f = 0.46875F;
        Vec3 vec3 = Vec3.atCenterOf(pos).relative(direction, -0.46875);
        float f1 = hasFramedMap ? 1.0F : 0.75F;
        float f2 = hasFramedMap ? 1.0F : 0.75F;
        Direction.Axis axis = direction.getAxis();
        double d = axis == Direction.Axis.X ? 0.0625 : f1;
        double d1 = axis == Direction.Axis.Y ? 0.0625 : f2;
        double d2 = axis == Direction.Axis.Z ? 0.0625 : f1;
        return AABB.ofSize(vec3, d, d1, d2);
    }
}

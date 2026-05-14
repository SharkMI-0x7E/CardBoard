package org.bukkit.craftbukkit.util;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.world.phys.AABB;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.VoxelShape;

public class CraftVoxelShape implements VoxelShape {

	private final net.minecraft.world.phys.shapes.VoxelShape shape;

    public CraftVoxelShape(net.minecraft.world.phys.shapes.VoxelShape shape) {
        this.shape = shape;
    }

    @Override
    public Collection<BoundingBox> getBoundingBoxes() {
        List<AABB> boxes = this.shape.toAabbs();
        ArrayList<BoundingBox> craftBoxes = new ArrayList<BoundingBox>(boxes.size());
        for (AABB aabb : boxes) {
            craftBoxes.add(new BoundingBox(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ));
        }
        return craftBoxes;
    }

    @Override
    public boolean overlaps(BoundingBox other) {
        Preconditions.checkArgument((other != null ? 1 : 0) != 0, "Other cannot be null");
        for (BoundingBox box : this.getBoundingBoxes()) {
            if (!box.overlaps(other)) continue;
            return true;
        }
        return false;
    }

}

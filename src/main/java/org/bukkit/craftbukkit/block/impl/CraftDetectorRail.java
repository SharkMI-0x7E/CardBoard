package org.bukkit.craftbukkit.block.impl;

import com.google.common.base.Preconditions;
import io.papermc.paper.annotation.GeneratedClass;
import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.bukkit.block.data.type.RedstoneRail;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.jspecify.annotations.NullMarked;

import java.util.Set;

@NullMarked
@GeneratedClass
public class CraftDetectorRail extends CraftBlockData implements RedstoneRail {
    private static final BooleanProperty POWERED = DetectorRailBlock.POWERED;

    private static final EnumProperty<RailShape> SHAPE = DetectorRailBlock.SHAPE;

    private static final BooleanProperty WATERLOGGED = DetectorRailBlock.WATERLOGGED;

    public CraftDetectorRail(BlockState state) {
        super(state);
    }

    @Override
    public boolean isPowered() {
        return this.get(POWERED);
    }

    @Override
    public void setPowered(final boolean powered) {
        this.set(POWERED, powered);
    }

    @Override
    public Shape getShape() {
        return this.get(SHAPE, Shape.class);
    }

    @Override
    public void setShape(final Shape shape) {
        Preconditions.checkArgument(shape != null, "shape cannot be null!");
        Preconditions.checkArgument(shape != Shape.NORTH_EAST && shape != Shape.NORTH_WEST && shape != Shape.SOUTH_EAST && shape != Shape.SOUTH_WEST, "Invalid rail shape, only straight rail are allowed for this property!");
        this.set(SHAPE, shape);
    }

    @Override
    public Set<Shape> getShapes() {
        return this.getValues(SHAPE, Shape.class);
    }

    @Override
    public boolean isWaterlogged() {
        return this.get(WATERLOGGED);
    }

    @Override
    public void setWaterlogged(final boolean waterlogged) {
        this.set(WATERLOGGED, waterlogged);
    }
}

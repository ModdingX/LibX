package io.github.noeppi_noeppi.libx.block;

import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds 4 different VoxelShapes, one for each horizontal facing. Those are all created by rotating
 * one original VoxelShape.
 */
public class RotationShape {

    protected final VoxelShape north;
    protected final VoxelShape south;
    protected final VoxelShape east;
    protected final VoxelShape west;

    /**
     * Creates a new RotationShape with the given base shape. The base shape should be the shape
     * facing north.
     */
    public RotationShape(VoxelShape baseShape) {
        this.north = baseShape.simplify();
        this.east = rotated(this.north);
        this.south = rotated(this.east);
        this.west = rotated(this.south);
    }

    /**
     * Gets the VoxelShape for the given direction. If the direction is not a horizontal
     * direction the base shape is returned.
     */
    public VoxelShape getShape(Direction direction) {
        switch (direction) {
            case SOUTH:
                return this.south;
            case WEST:
                return this.west;
            case EAST:
                return this.east;
            case NORTH:
            default:
                return this.north;
        }
    }

    private static VoxelShape rotated(VoxelShape src) {
        List<VoxelShape> boxes = new ArrayList<>();
        src.forEachBox((fromX, fromY, fromZ, toX, toY, toZ) -> boxes.add(VoxelShapes.create(1 - fromZ, fromY, fromX, 1 - toZ, toY, toX)));
        return VoxelShapes.or(VoxelShapes.empty(), boxes.toArray(new VoxelShape[]{})).simplify();
    }
}

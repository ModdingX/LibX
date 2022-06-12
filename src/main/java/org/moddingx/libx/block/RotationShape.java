package org.moddingx.libx.block;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds 4 different {@link VoxelShape VoxelShapes}, one for each horizontal facing. Those are all created by rotating
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
        this.north = baseShape.optimize();
        this.east = rotated(this.north);
        this.south = rotated(this.east);
        this.west = rotated(this.south);
    }

    /**
     * Gets the VoxelShape for the given direction. If the direction is not a horizontal
     * direction the base shape is returned.
     */
    public VoxelShape getShape(Direction direction) {
        return switch (direction) {
            case SOUTH -> this.south;
            case WEST -> this.west;
            case EAST -> this.east;
            default -> this.north;
        };
    }

    private static VoxelShape rotated(VoxelShape src) {
        List<VoxelShape> boxes = new ArrayList<>();
        src.forAllBoxes((fromX, fromY, fromZ, toX, toY, toZ) -> boxes.add(Shapes.box(
                Math.min(1 - fromZ, 1 - toZ),
                Math.min(fromY, toY),
                Math.min(fromX, toX),
                Math.max(1 - fromZ, 1 - toZ),
                Math.max(fromY, toY),
                Math.max(fromX, toX)
        )));
        return Shapes.or(Shapes.empty(), boxes.toArray(new VoxelShape[]{})).optimize();
    }
}

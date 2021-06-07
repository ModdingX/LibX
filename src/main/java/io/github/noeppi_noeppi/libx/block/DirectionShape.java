package io.github.noeppi_noeppi.libx.block;

import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds 6 different VoxelShapes, one for each facing. Those are all created by rotating
 * one original VoxelShape.
 */
public class DirectionShape extends RotationShape {
    
    protected final VoxelShape up;
    protected final VoxelShape down;

    /**
     * Creates a new RotationShape with the given base shape. The base shape should be the shape
     * facing up.
     */
    public DirectionShape(VoxelShape baseShape) {
        super(rotatedV(baseShape));
        this.up = baseShape;
        this.down = rotatedV(this.north);
    }

    /**
     * @inheritDoc
     */
    @Override
    public VoxelShape getShape(Direction direction) {
        switch (direction) {
            case UP:
                return this.up;
            case DOWN:
                return this.down;
            default:
                return super.getShape(direction);
        }
    }

    private static VoxelShape rotatedV(VoxelShape src) {
        List<VoxelShape> boxes = new ArrayList<>();
        src.forEachBox((fromX, fromY, fromZ, toX, toY, toZ) -> boxes.add(VoxelShapes.create(fromX, fromZ, 1 - fromY, toX, toZ, 1 - toY)));
        return VoxelShapes.or(VoxelShapes.empty(), boxes.toArray(new VoxelShape[]{})).simplify();
    }
}

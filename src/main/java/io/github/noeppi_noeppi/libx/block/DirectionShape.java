package io.github.noeppi_noeppi.libx.block;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds 6 different {@link VoxelShape VoxelShapes}, one for each facing. Those are all created by rotating
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
        return switch (direction) {
            case UP -> this.up;
            case DOWN -> this.down;
            default -> super.getShape(direction);
        };
    }

    private static VoxelShape rotatedV(VoxelShape src) {
        List<VoxelShape> boxes = new ArrayList<>();
        src.forAllBoxes((fromX, fromY, fromZ, toX, toY, toZ) -> boxes.add(Shapes.box(fromX, fromZ, 1 - fromY, toX, toZ, 1 - toY)));
        return Shapes.or(Shapes.empty(), boxes.toArray(new VoxelShape[]{})).optimize();
    }
}

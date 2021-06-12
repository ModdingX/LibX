package io.github.noeppi_noeppi.libx.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

/**
 * Utilities for instances of {@link AxisAlignedBB}. 
 */
public class BoundingBoxUtils {

    /**
     * Creates a new {@link AxisAlignedBB} that centers at the given entity
     * and has the given radius in all directions.
     */
    public static AxisAlignedBB expand(Entity center, double radius) {
        return expand(center.getPositionVec(), radius);
    }
    
    /**
     * Creates a new {@link AxisAlignedBB} that centers at the given vector
     * and has the given radius in all directions.
     */
    public static AxisAlignedBB expand(Vector3d center, double radius) {
        return new AxisAlignedBB(center.getX() - radius, center.getY() - radius, center.getZ() - radius,
                center.getX() + radius, center.getY() + radius, center.getZ() + radius);
    }
}

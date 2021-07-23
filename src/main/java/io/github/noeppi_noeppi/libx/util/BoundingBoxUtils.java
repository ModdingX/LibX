package io.github.noeppi_noeppi.libx.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Utilities for instances of {@link AxisAlignedBB}. 
 */
public class BoundingBoxUtils {

    /**
     * Creates a new {@link AxisAlignedBB} that centers at the given entity
     * and has the given radius in all directions.
     */
    public static AABB expand(Entity center, double radius) {
        return expand(center.position(), radius);
    }
    
    /**
     * Creates a new {@link AxisAlignedBB} that centers at the given vector
     * and has the given radius in all directions.
     */
    public static AABB expand(Vec3 center, double radius) {
        return new AABB(center.x() - radius, center.y() - radius, center.z() - radius,
                center.x() + radius, center.y() + radius, center.z() + radius);
    }
}

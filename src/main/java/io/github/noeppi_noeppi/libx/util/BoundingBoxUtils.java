package io.github.noeppi_noeppi.libx.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

// TODO add javadoc
public class BoundingBoxUtils {
    
    public static AxisAlignedBB expand(Entity center, double radius) {
        return expand(center.getPositionVec(), radius);
    }
    
    public static AxisAlignedBB expand(Vector3d center, double radius) {
        return new AxisAlignedBB(center.getX() - radius, center.getY() - radius, center.getZ() - radius,
                center.getX() + radius, center.getY() + radius, center.getZ() + radius);
    }
}

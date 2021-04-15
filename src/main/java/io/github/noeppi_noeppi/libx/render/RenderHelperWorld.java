package io.github.noeppi_noeppi.libx.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

/**
 * Utilities for rendering in the world.
 */
public class RenderHelperWorld {

    /**
     * This is meant to be called in {@code RenderWorldLastEvent}. This will move the matrix stack to the
     * given position in the world. Do not always use this with {@code (0, 0, 0)} and translate to the
     * position you need afterwards as it will be buggy millions of blocks away because of rounding errors.
     */
    public static void loadProjection(MatrixStack matrixStack, BlockPos pos) {
        loadProjection(matrixStack, pos.getX(), pos.getY(), pos.getZ());
    }
    
    /**
     * This is meant to be called in {@code RenderWorldLastEvent}. This will move the matrix stack to the
     * given position in the world. Do not always use this with {@code (0, 0, 0)} and translate to the
     * position you need afterwards as it will be buggy millions of blocks away because of rounding errors.
     */
    public static void loadProjection(MatrixStack matrixStack, Vector3d pos) {
        loadProjection(matrixStack, pos.x, pos.y, pos.z);
    }

    /**
     * This is meant to be called in {@code RenderWorldLastEvent}. This will move the matrix stack to the
     * given position in the world. Do not always use this with {@code (0, 0, 0)} and translate to the
     * position you need afterwards as it will be buggy millions of blocks away because of rounding errors.
     */
    public static void loadProjection(MatrixStack matrixStack, double x, double y, double z) {
        Vector3d projection = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        matrixStack.translate(x - projection.x, y - projection.y, z - projection.z);
    }
}

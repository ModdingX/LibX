package io.github.noeppi_noeppi.libx.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;

/**
 * Utilities for rendering in the world.
 */
public class RenderHelperWorld {

    /**
     * This is meant to be called in {@link RenderWorldLastEvent}. This will move the matrix stack to the
     * given position in the world. Do not always use this with {@code (0, 0, 0)} and translate to the
     * position you need afterwards as it will be buggy millions of blocks away because of rounding errors.
     */
    public static void loadProjection(PoseStack poseStack, BlockPos pos) {
        loadProjection(poseStack, pos.getX(), pos.getY(), pos.getZ());
    }
    
    /**
     * This is meant to be called in {@link RenderWorldLastEvent}. This will move the matrix stack to the
     * given position in the world. Do not always use this with {@code (0, 0, 0)} and translate to the
     * position you need afterwards as it will be buggy millions of blocks away because of rounding errors.
     */
    public static void loadProjection(PoseStack poseStack, Vec3 pos) {
        loadProjection(poseStack, pos.x, pos.y, pos.z);
    }

    /**
     * This is meant to be called in {@link RenderWorldLastEvent}. This will move the matrix stack to the
     * given position in the world. Do not always use this with {@code (0, 0, 0)} and translate to the
     * position you need afterwards as it will be buggy millions of blocks away because of rounding errors.
     */
    public static void loadProjection(PoseStack poseStack, double x, double y, double z) {
        Vec3 projection = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        poseStack.translate(x - projection.x, y - projection.y, z - projection.z);
    }
}

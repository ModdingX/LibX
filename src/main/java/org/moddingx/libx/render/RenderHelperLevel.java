package org.moddingx.libx.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;

/**
 * Utilities for rendering in the level.
 */
public class RenderHelperLevel {

    /**
     * This is meant to be called in {@link RenderLevelStageEvent}. This will move the pose stack to the
     * given position in the world. Do not always use this with {@code (0, 0, 0)} and translate to the
     * position you need afterwards as it will be buggy millions of blocks away because of rounding errors.
     */
    public static void loadCameraPosition(Camera camera, PoseStack poseStack, BlockPos pos) {
        loadCameraPosition(camera, poseStack, pos.getX(), pos.getY(), pos.getZ());
    }
    
    /**
     * This is meant to be called in {@link RenderLevelStageEvent}. This will move the pose stack to the
     * given position in the world. Do not always use this with {@code (0, 0, 0)} and translate to the
     * position you need afterwards as it will be buggy millions of blocks away because of rounding errors.
     */
    public static void loadCameraPosition(Camera camera, PoseStack poseStack, Vec3 pos) {
        loadCameraPosition(camera, poseStack, pos.x, pos.y, pos.z);
    }

    /**
     * This is meant to be called in {@link RenderLevelStageEvent}. This will move the pose stack to the
     * given position in the world. Do not always use this with {@code (0, 0, 0)} and translate to the
     * position you need afterwards as it will be buggy millions of blocks away because of rounding errors.
     */
    public static void loadCameraPosition(Camera camera, PoseStack poseStack, double x, double y, double z) {
        Vec3 cameraPos = camera.getPosition();
        poseStack.translate(x - cameraPos.x, y - cameraPos.y, z - cameraPos.z);
    }
}

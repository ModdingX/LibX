package org.moddingx.libx.impl.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.moddingx.libx.render.RenderHelper;
import org.moddingx.libx.render.target.RenderJob;
import org.moddingx.libx.render.target.RenderJobFailedException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class JobRenderer {
    
    public static NativeImage renderJob(RenderJob job) {
        int width = job.width();
        int height = job.height();
        boolean overlay = job.usesOverlay();
        
        int maxTextureSize = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
        if (width > maxTextureSize || height > maxTextureSize) {
            throw new RenderJobFailedException(RenderJobFailedException.Reason.TEXTURE_TOO_LARGE, "Maximum texture size exceeded: " + width + "x" + height + ", maximum is " + maxTextureSize + "x" + maxTextureSize);
        }
        
        RenderTarget target = new TextureTarget(width, height, true, Minecraft.ON_OSX);
        
        target.setClearColor(0, 0, 0, 0);
        target.clear(true);

        resetDepthState();

        // Clear buffer
        target.bindWrite(true);
        RenderSystem.clear(0x4100, Minecraft.ON_OSX);
        
        // Render main scene
        target.bindWrite(true);
        
        FogRenderer.setupNoFog();
        
        RenderSystem.enableTexture();
        RenderSystem.enableCull();
        
        RenderSystem.viewport(0, 0, width, height);

        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.setIdentity();
        modelViewStack.mulPoseMatrix(job.setupModelViewMatrix());
        RenderSystem.applyModelViewMatrix();

        Matrix4f projectionMatrix = job.setupProjectionMatrix();
        RenderSystem.setProjectionMatrix(projectionMatrix);
        
        Lighting.setupFor3DItems();
        RenderSystem.defaultBlendFunc();
        RenderHelper.resetColor();

        @Nullable
        Matrix4f transformationMatrix = overlay ? new Matrix4f(modelViewStack.last().pose()) : null;
        PoseStack poseStack = new PoseStack();
        job.setupTransformation(poseStack);
        if (overlay) {
            transformationMatrix.mul(poseStack.last().pose());
        }
        
        RenderBuffers buffers = new RenderBuffers();
        job.render(poseStack, buffers.bufferSource());
        buffers.bufferSource().endBatch();
        
        if (overlay) {
            // Render overlay
            target.bindWrite(true);
            FogRenderer.setupNoFog();
            resetDepthState();

            RenderSystem.viewport(0, 0, width, height);
            modelViewStack.setIdentity();
            modelViewStack.mulPoseMatrix(job.setupModelViewMatrix());
            RenderSystem.applyModelViewMatrix();

            RenderSystem.setProjectionMatrix(new Matrix4f().ortho(0, width, 0, height, 1000, 3000));

            PoseStack overlayPoseStack = new PoseStack();
            Lighting.setupFor3DItems();

            RenderJob.Projector projector = new ProjectorImpl(projectionMatrix, transformationMatrix, 0, 0, width, height);
            job.renderOverlay(overlayPoseStack, buffers.bufferSource(), projector);
            buffers.bufferSource().endBatch();
        }
        
        resetDepthState();
        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
        
        NativeImage img = takeNonOpaqueScreenshot(target);
        target.unbindWrite();
        return img;
    }
    
    private static void resetDepthState() {
        RenderSystem.clearDepth(1);
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        GL11.glFrontFace(GL11.GL_CCW);
    }

    // See Screenshot.takeScreenshot
    private static NativeImage takeNonOpaqueScreenshot(RenderTarget fb) {
        NativeImage img = new NativeImage(fb.width, fb.height, false);
        RenderSystem.bindTexture(fb.getColorTextureId());
        img.downloadTexture(0, false);
        img.flipY();
        return img;
    }
    
    private static class ProjectorImpl implements RenderJob.Projector {
        
        private final Matrix4f projection;
        private final Matrix4f transformation;
        private final int viewportX;
        private final int viewportY;
        private final int viewportWidth;
        private final int viewportHeight;

        public ProjectorImpl(@Nonnull Matrix4f projection, @Nonnull Matrix4f transformation, int viewportX, int viewportY, int viewportWidth, int viewportHeight) {
            this.projection = projection;
            this.transformation = transformation;
            this.viewportX = viewportX;
            this.viewportY = viewportY;
            this.viewportWidth = viewportWidth;
            this.viewportHeight = viewportHeight;
        }
        
        @Override
        public Vec2 projectPoint(Vector3f point) {
            Vector4f vec4 = new Vector4f(point, 1);
            this.transformation.transform(vec4);
            this.projection.transform(vec4);
            if (!Double.isNaN(vec4.w()) && !Double.isInfinite(vec4.w()) && Math.abs(vec4.w()) >= 1.0E-6F) {
                vec4.set(vec4.x() / vec4.w(), vec4.y() / vec4.w(), vec4.z() / vec4.w(), 1);
            }
            float wx = this.viewportX + (this.viewportWidth * ((vec4.x() + 1) / 2));
            float wy = this.viewportY + (this.viewportHeight * (1 - ((vec4.y() + 1) / 2)));
            return new Vec2(wx, wy);
        }
    }
}

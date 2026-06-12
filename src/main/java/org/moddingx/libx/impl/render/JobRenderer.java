package org.moddingx.libx.impl.render;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PerspectiveProjectionMatrixBuffer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.moddingx.libx.render.RenderHelper;
import org.moddingx.libx.render.target.RenderJob;
import org.moddingx.libx.render.target.RenderJobFailedException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class JobRenderer {

    private static final OutputTarget[] OUTPUT_TARGETS = new OutputTarget[]{
            OutputTarget.MAIN_TARGET,
            OutputTarget.OUTLINE_TARGET,
            OutputTarget.WEATHER_TARGET,
            OutputTarget.ITEM_ENTITY_TARGET
    };

    // Hardcoded GUI Z range constants (from GuiRenderer, previously GuiGraphics.MIN_GUI_Z / MAX_GUI_Z)
    public static final float GUI_MIN_Z = 0.0F;
    public static final float GUI_MAX_Z = 10000.0F;
    public static final float GUI_Z_NEAR = 1000.0F;

    public static void renderJob(RenderJob job, CompletableFuture<NativeImage> future) {
        int width = job.width();
        int height = job.height();
        boolean overlay = job.usesOverlay();

        int maxTextureSize = RenderSystem.getDevice().getMaxTextureSize();
        if (width > maxTextureSize || height > maxTextureSize) {
            throw new RenderJobFailedException(RenderJobFailedException.Reason.TEXTURE_TOO_LARGE, "Maximum texture size exceeded: " + width + "x" + height + ", maximum is " + maxTextureSize + "x" + maxTextureSize);
        }

        RenderTarget target = new TextureTarget("LibX render job", width, height, true);

        // Clear color and depth
        RenderSystem.getDevice().createCommandEncoder()
                .clearColorAndDepthTextures(target.getColorTexture(), 0x00000000, target.getDepthTexture(), 1.0);

        // Save fog and projection state to restore after the render job
        GpuBufferSlice savedFog = RenderSystem.getShaderFog();
        GpuBufferSlice savedProjMatrix = RenderSystem.getProjectionMatrixBuffer();
        ProjectionType savedProjType = RenderSystem.getProjectionType();

        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.identity();
        modelViewStack.mul(job.setupModelViewMatrix());

        Matrix4f projectionMatrix = job.setupProjectionMatrix();

        // PerspectiveProjectionMatrixBuffer converts a Matrix4f into a GPU uniform buffer
        try (PerspectiveProjectionMatrixBuffer projBuffer = new PerspectiveProjectionMatrixBuffer("LibX render job")) {
            RenderSystem.setProjectionMatrix(projBuffer.getBuffer(projectionMatrix), job.getProjectionType());

            Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);
            RenderHelper.resetColor();

            @Nullable
            Matrix4f transformationMatrix = overlay ? new Matrix4f(modelViewStack) : null;
            PoseStack poseStack = new PoseStack();
            job.setupTransformation(poseStack);
            if (overlay) {
                transformationMatrix.mul(poseStack.last().pose());
            }

            Minecraft mc = Minecraft.getInstance();
            RenderTarget previousMainRenderTarget = mc.mainRenderTarget;
            mc.mainRenderTarget = target;

            boolean screenshotScheduled = false;
            try {
                RenderBuffers buffers = new RenderBuffers(Runtime.getRuntime().availableProcessors());
                job.render(poseStack, buffers.bufferSource());
                buffers.bufferSource().endBatch();

                if (overlay) {
                    modelViewStack.identity();
                    modelViewStack.mul(new Matrix4f().translate(0, 0, GUI_Z_NEAR - GUI_MIN_Z));

                    Matrix4f orthoMatrix = new Matrix4f().setOrtho(0, width, height, 0, GUI_Z_NEAR, GUI_Z_NEAR + GUI_MAX_Z - GUI_MIN_Z);
                    RenderSystem.setProjectionMatrix(projBuffer.getBuffer(orthoMatrix), ProjectionType.ORTHOGRAPHIC);

                    PoseStack overlayPoseStack = new PoseStack();
                    Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);

                    RenderJob.Projector projector = new ProjectorImpl(projectionMatrix, transformationMatrix, 0, 0, width, height);
                    job.renderOverlay(overlayPoseStack, buffers.bufferSource(), projector);
                    buffers.bufferSource().endBatch();
                }

                takeNonOpaqueScreenshot(target, future);
                screenshotScheduled = true;
            } finally {
                mc.mainRenderTarget = previousMainRenderTarget;
                modelViewStack.popMatrix();
                RenderSystem.setShaderFog(savedFog);
                RenderSystem.setProjectionMatrix(savedProjMatrix, savedProjType);
                if (!screenshotScheduled) {
                    target.destroyBuffers();
                }
            }
        }
    }

    // See Screenshot.takeScreenshot - but preserves alpha (no | 0xFF000000)
    private static void takeNonOpaqueScreenshot(RenderTarget fb, CompletableFuture<NativeImage> future) {
        int width = fb.width;
        int height = fb.height;
        GpuTexture texture = fb.getColorTexture();
        if (texture == null) {
            fb.destroyBuffers();
            future.completeExceptionally(new IllegalStateException("Tried to capture screenshot of an incomplete framebuffer"));
            return;
        }
        int pixelSize = texture.getFormat().pixelSize();
        GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(
                () -> "LibX render job screenshot", GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_MAP_READ, width * height * pixelSize);
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        RenderSystem.getDevice().createCommandEncoder().copyTextureToBuffer(texture, gpuBuffer, 0, () -> {
            try {
                try (GpuBuffer.MappedView view = encoder.mapBuffer(gpuBuffer, true, false)) {
                    NativeImage img = new NativeImage(width, height, false);
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            int color = view.data().getInt((x + y * width) * pixelSize);
                            img.setPixelABGR(x, height - y - 1, color);
                        }
                    }
                    future.complete(img);
                }
            } catch (Exception e) {
                future.completeExceptionally(e);
            } finally {
                gpuBuffer.close();
                fb.destroyBuffers();
            }
        }, 0);
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

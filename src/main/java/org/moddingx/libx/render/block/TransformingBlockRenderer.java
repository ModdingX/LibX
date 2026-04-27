package org.moddingx.libx.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A {@link BlockEntityRenderer} that transforms the {@link PoseStack pose stack} in some way before
 * the actual render code. The render helper methods take care of the {@link PoseStack pose stack}
 * being pushed and popped.
 *
 * @param <T> The block entity type.
 */
public abstract class TransformingBlockRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, TransformingBlockRenderer.RenderState<T>> {

    @Nonnull
    @Override
    public RenderState<T> createRenderState() {
        return new RenderState<>();
    }

    @Override
    public void extractRenderState(@Nonnull T blockEntity, @Nonnull RenderState<T> renderState, float partialTicks, @Nonnull Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTicks, cameraPos, breakProgress);
        renderState.blockEntity = blockEntity;
        renderState.partialTicks = partialTicks;
        renderState.cameraPos = cameraPos;
    }

    @Override
    public final void submit(@Nonnull RenderState<T> renderState, @Nonnull PoseStack poseStack, @Nonnull SubmitNodeCollector nodeCollector, @Nonnull CameraRenderState cameraRenderState) {
        if (renderState.blockEntity == null) return;
        poseStack.pushPose();
        this.transform(renderState.blockEntity, renderState.partialTicks, poseStack);
        this.doRender(renderState.blockEntity, renderState.partialTicks, poseStack, nodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, renderState.cameraPos);
        poseStack.popPose();
    }

    /**
     * Applies the pre-render transformation to the {@link PoseStack pose stack}.
     */
    protected abstract void transform(@Nonnull T blockEntity, float partialTicks, @Nonnull PoseStack poseStack);

    /**
     * Renders the {@link BlockEntity block entity}.
     */
    protected abstract void doRender(@Nonnull T blockEntity, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull SubmitNodeCollector nodeCollector, int light, int overlay, @Nonnull Vec3 cameraPos);

    public static class RenderState<T extends BlockEntity> extends BlockEntityRenderState {
        @Nullable
        public T blockEntity;
        public float partialTicks;
        @Nonnull
        public Vec3 cameraPos = Vec3.ZERO;
    }
}

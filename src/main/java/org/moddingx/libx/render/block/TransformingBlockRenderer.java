package org.moddingx.libx.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

/**
 * A {@link BlockEntityRenderer} that transforms the {@link PoseStack pose stack} in some way before
 * the actual render code. The render helper methods take care of the {@link PoseStack pose stack}
 * being pushed and popped.
 *
 * @param <T> The block entity type.
 * @param <S> The render state type. Extend {@link RenderState} and return it from
 *            {@link #createRenderState()} to carry additional extracted values.
 */
public abstract class TransformingBlockRenderer<T extends BlockEntity, S extends TransformingBlockRenderer.RenderState> implements BlockEntityRenderer<T, S> {

    @Override
    @OverridingMethodsMustInvokeSuper
    public void extractRenderState(@Nonnull T blockEntity, @Nonnull S renderState, float partialTicks, @Nonnull Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTicks, cameraPos, breakProgress);
        renderState.blockState = blockEntity.getBlockState();
        renderState.partialTicks = partialTicks;
        renderState.cameraPos = cameraPos;
    }

    @Override
    public final void submit(@Nonnull S renderState, @Nonnull PoseStack poseStack, @Nonnull SubmitNodeCollector nodeCollector, @Nonnull CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        try {
            this.transform(renderState, poseStack);
            this.doRender(renderState, poseStack, nodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY);
        } finally {
            poseStack.popPose();
        }
    }

    /**
     * Applies the pre-render transformation to the {@link PoseStack pose stack}.
     */
    protected abstract void transform(@Nonnull S renderState, @Nonnull PoseStack poseStack);

    /**
     * Renders the extracted {@link RenderState render state}.
     */
    protected abstract void doRender(@Nonnull S renderState, @Nonnull PoseStack poseStack, @Nonnull SubmitNodeCollector nodeCollector, int light, int overlay);

    public static class RenderState extends BlockEntityRenderState {

        /**
         * The {@link BlockState block state} of the block entity at extraction time.
         * {@link BlockEntityRenderState} stores one as well, but does not expose it.
         */
        @Nonnull
        public BlockState blockState = Blocks.AIR.defaultBlockState();

        public float partialTicks;

        @Nonnull
        public Vec3 cameraPos = Vec3.ZERO;
    }
}

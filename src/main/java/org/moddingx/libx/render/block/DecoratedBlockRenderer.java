package org.moddingx.libx.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;

/**
 * A {@link BlockEntityRenderer} that transforms the {@link PoseStack pose stack} in some way before
 * the actual render code. The {@link #render(BlockEntity, float, PoseStack, MultiBufferSource, int, int) render}
 * methode takes care of the {@link PoseStack pose stack} being pushed and popped.
 * @param <T>
 */
public abstract class DecoratedBlockRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {

    @Override
    public final void render(@Nonnull T blockEntity, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int light, int overlay) {
        poseStack.pushPose();
        this.decorate(blockEntity, partialTicks, poseStack);
        this.doRender(blockEntity, partialTicks, poseStack, buffer, light, overlay);
        poseStack.popPose();
    }

    /**
     * Applies the pre-redner transformation to the {@link PoseStack pose stack}.
     */
    protected abstract void decorate(@Nonnull T blockEntity, float partialTicks, @Nonnull PoseStack poseStack);
    
    /**
     * Renders the {@link BlockEntity block entity}.
     */
    protected abstract void doRender(@Nonnull T blockEntity, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int light, int overlay);
}

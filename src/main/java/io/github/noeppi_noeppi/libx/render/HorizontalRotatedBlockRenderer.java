package io.github.noeppi_noeppi.libx.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nonnull;

/**
 * A {@link BlockEntityRenderer} that before calling the actual render code rotates the {@link PoseStack} depending
 * on the horizontal facing. This may only be used with blocks that have the property
 * {@link BlockStateProperties#HORIZONTAL_FACING}
 */
public abstract class HorizontalRotatedBlockRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {

    @Override
    public final void render(@Nonnull T blockEntity, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int light, int overlay) {
        poseStack.pushPose();
        float f = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() + 180;
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-f));
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        this.doRender(blockEntity, partialTicks, poseStack, buffer, light, overlay);
        poseStack.popPose();
    }

    /**
     * The custom render code goes in here.
     */
    protected abstract void doRender(@Nonnull T blockEntity, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int light, int overlay);
}

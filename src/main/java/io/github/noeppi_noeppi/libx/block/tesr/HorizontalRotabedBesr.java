package io.github.noeppi_noeppi.libx.block.tesr;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.mojang.math.Vector3f;

import javax.annotation.Nonnull;

/**
 * A TileEntityRenderer that before calling the actual render code rotates the MatrixStack depending
 * on the horizontal facing. This may only be used with blocks that have the property
 * {@link BlockStateProperties#HORIZONTAL_FACING}
 */
public abstract class HorizontalRotabedBesr<T extends BlockEntity> implements BlockEntityRenderer<T> {

    @Override
    public final void render(@Nonnull T blockEntity, float partialTicks, @Nonnull PoseStack matrixStack, @Nonnull MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        matrixStack.pushPose();
        float f = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() + 180;
        matrixStack.translate(0.5D, 0.5D, 0.5D);
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(-f));
        matrixStack.translate(-0.5D, -0.5D, -0.5D);
        this.doRender(blockEntity, partialTicks, matrixStack, buffer, combinedLight, combinedOverlay);
        matrixStack.popPose();
    }

    /**
     * The custom render code goes in here.
     */
    protected abstract void doRender(@Nonnull T tile, float partialTicks, @Nonnull PoseStack matrixStack, @Nonnull MultiBufferSource buffer, int light, int overlay);
}

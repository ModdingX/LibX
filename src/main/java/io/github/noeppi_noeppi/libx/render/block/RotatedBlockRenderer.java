package io.github.noeppi_noeppi.libx.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nonnull;

/**
 * A {@link BlockEntityRenderer} that before calling the actual render code rotates the {@link PoseStack} depending
 * on the horizontal facing. This may only be used with blocks that have the property {@link BlockStateProperties#HORIZONTAL_FACING}.
 * The {@link BlockEntity block entity} should be rendered as if it was facing north.
 */
public abstract class RotatedBlockRenderer<T extends BlockEntity> extends DecoratedBlockRenderer<T> {

    @Override
    protected final void decorate(@Nonnull T blockEntity, float partialTicks, @Nonnull PoseStack poseStack) {
        float rot = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() + 180;
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-rot));
        poseStack.translate(-0.5D, -0.5D, -0.5D);
    }
}

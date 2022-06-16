package org.moddingx.libx.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nonnull;

/**
 * A {@link BlockEntityRenderer} that before calling the actual render code rotates the {@link PoseStack}
 * depending on the facing. This may only be used with blocks that have the property {@link BlockStateProperties#FACING}.
 * The {@link BlockEntity block entity} should be rendered as if it was facing up.
 */
public abstract class DirectionalBlockRenderer<T extends BlockEntity> extends TransformingBlockRenderer<T> {

    @Override
    protected final void transform(@Nonnull T blockEntity, float partialTicks, @Nonnull PoseStack poseStack) {
        Direction facing = blockEntity.getBlockState().getValue(BlockStateProperties.FACING);
        float xRot = switch (facing) {
            case UP -> 0;
            case DOWN -> 180;
            default -> 90;
        };
        float yRot = facing.getAxis() == Direction.Axis.Y ? 0 : facing.toYRot();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(xRot));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(yRot));
        poseStack.translate(-0.5D, -0.5D, -0.5D);
    }
}

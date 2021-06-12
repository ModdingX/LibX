package io.github.noeppi_noeppi.libx.block.tesr;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nonnull;

/**
 * A TileEntityRenderer that before calling the actual render code rotates the MatrixStack depending
 * on the horizontal facing. This may only be used with blocks that have the property
 * {@link BlockStateProperties#HORIZONTAL_FACING}
 */
public abstract class HorizontalRotatedTesr<T extends TileEntity> extends TileEntityRenderer<T> {

    public HorizontalRotatedTesr(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public final void render(@Nonnull T tile, float partialTicks, @Nonnull MatrixStack matrixStack, @Nonnull IRenderTypeBuffer buffer, int light, int overlay) {
        matrixStack.push();
        float f = tile.getBlockState().get(BlockStateProperties.HORIZONTAL_FACING).getHorizontalAngle() + 180;
        matrixStack.translate(0.5D, 0.5D, 0.5D);
        matrixStack.rotate(Vector3f.YP.rotationDegrees(-f));
        matrixStack.translate(-0.5D, -0.5D, -0.5D);
        this.doRender(tile, partialTicks, matrixStack, buffer, light, overlay);
        matrixStack.pop();
    }

    /**
     * The custom render code goes in here.
     */
    protected abstract void doRender(@Nonnull T tile, float partialTicks, @Nonnull MatrixStack matrixStack, @Nonnull IRenderTypeBuffer buffer, int light, int overlay);
}

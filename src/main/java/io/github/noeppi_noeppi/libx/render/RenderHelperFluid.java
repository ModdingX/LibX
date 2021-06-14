package io.github.noeppi_noeppi.libx.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraftforge.fluids.FluidStack;

/**
 * Helper class to render fluids into a gui. This can either render {@link FluidStack fluid stacks} or {@link Fluid fluids} with a special color.
 */
public class RenderHelperFluid {

    public static void renderFluid(MatrixStack matrixStack, IRenderTypeBuffer buffer, FluidStack stack, int x, int y, int width, int height) {
        if (!stack.isEmpty()) {
            Fluid fluid = stack.getFluid();
            int color = fluid.getAttributes().getColor(stack);
            TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(fluid.getAttributes().getStillTexture(stack));
            renderFluid(matrixStack, buffer, sprite, color, x, y, width, height);
        }
    }

    public static void renderFluid(MatrixStack matrixStack, IRenderTypeBuffer buffer, int color, int x, int y, int width, int height) {
        Fluid fluid = Fluids.WATER;
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(fluid.getAttributes().getStillTexture());
        renderFluid(matrixStack, buffer, sprite, color, x, y, width, height);
    }

    private static void renderFluid(MatrixStack matrixStack, IRenderTypeBuffer buffer, TextureAtlasSprite sprite, int color, int x, int y, int width, int height) {
        matrixStack.push();
        matrixStack.translate(0, 0, 100);
        Minecraft.getInstance().getTextureManager().bindTexture(sprite.getAtlasTexture().getTextureLocation());
        RenderHelper.color(color);
        RenderHelper.repeatBlit(matrixStack, x, y, width, height, sprite);
        RenderHelper.resetColor();
        matrixStack.pop();
    }
}

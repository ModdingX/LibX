package org.moddingx.libx.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Helper class to render fluids into a GUI. This can either render {@link FluidStack fluid stacks}
 * or {@link Fluid fluids} with a special color.
 */
public class RenderHelperFluid {

    private static final ResourceLocation BLOCK_ATLAS = ResourceLocation.withDefaultNamespace("textures/atlas/blocks.png");

    public static void renderFluid(GuiGraphics graphics, FluidStack stack, int x, int y, int width, int height) {
        if (!stack.isEmpty()) {
            Fluid fluid = stack.getFluid();
            IClientFluidTypeExtensions properties = IClientFluidTypeExtensions.of(fluid);
            int color = properties.getTintColor(stack);
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(BLOCK_ATLAS).apply(properties.getStillTexture(stack));
            renderFluid(graphics, sprite, color, x, y, width, height);
        }
    }

    public static void renderFluid(GuiGraphics graphics, int color, int x, int y, int width, int height) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(BLOCK_ATLAS).apply(IClientFluidTypeExtensions.of(Fluids.WATER).getStillTexture());
        renderFluid(graphics, sprite, color, x, y, width, height);
    }

    private static void renderFluid(GuiGraphics graphics, TextureAtlasSprite sprite, int color, int x, int y, int width, int height) {
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 100);
        // Some mods set alpha, other leave it 0, so we use the alpha whenever it is not 0.
        int alpha = (color >>> 24) & 0xFF;
        if (alpha > 0) {
            RenderHelper.argb(color);
        } else {
            RenderHelper.rgb(color);
        }
        RenderHelper.repeatBlit(RenderType::guiTextured, graphics, x, y, width, height, sprite);
        RenderHelper.resetColor();
        graphics.pose().popPose();
    }
}

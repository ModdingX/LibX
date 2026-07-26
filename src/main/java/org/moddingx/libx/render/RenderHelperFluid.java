package org.moddingx.libx.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Helper class to render fluids into a GUI. This can either render {@link FluidStack fluid stacks}
 * or {@link Fluid fluids} with a special color.
 */
public class RenderHelperFluid {

    public static void renderFluid(GuiGraphicsExtractor graphics, FluidStack stack, int x, int y, int width, int height) {
        if (!stack.isEmpty()) {
            FluidModel model = fluidModel(stack.getFluid());
            FluidTintSource tintSource = model.fluidTintSource();
            int color = tintSource == null ? 0xFFFFFFFF : tintSource.colorAsStack(stack);
            renderFluid(graphics, model.stillMaterial().sprite(), color, x, y, width, height);
        }
    }

    public static void renderFluid(GuiGraphicsExtractor graphics, int color, int x, int y, int width, int height) {
        renderFluid(graphics, fluidModel(Fluids.WATER).stillMaterial().sprite(), color, x, y, width, height);
    }

    private static FluidModel fluidModel(Fluid fluid) {
        return Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
    }

    private static void renderFluid(GuiGraphicsExtractor graphics, TextureAtlasSprite sprite, int color, int x, int y, int width, int height) {
        graphics.pose().pushMatrix();
        graphics.nextStratum();
        // Some mods set alpha, other leave it 0, so we use the alpha whenever it is not 0.
        int alpha = (color >>> 24) & 0xFF;
        if (alpha > 0) {
            RenderHelper.argb(color);
        } else {
            RenderHelper.rgb(color);
        }
        RenderHelper.repeatBlit(RenderPipelines.GUI_TEXTURED, graphics, x, y, width, height, sprite);
        RenderHelper.resetColor();
        graphics.pose().popMatrix();
    }
}

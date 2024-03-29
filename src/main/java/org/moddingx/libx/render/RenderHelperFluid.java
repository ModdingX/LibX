package org.moddingx.libx.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

/**
 * Helper class to render fluids into a GUI. This can either render {@link FluidStack fluid stacks}
 * or {@link Fluid fluids} with a special color.
 */
public class RenderHelperFluid {

    public static void renderFluid(GuiGraphics graphics, FluidStack stack, int x, int y, int width, int height) {
        if (!stack.isEmpty()) {
            Fluid fluid = stack.getFluid();
            IClientFluidTypeExtensions properties = IClientFluidTypeExtensions.of(fluid);
            int color = properties.getTintColor(stack);
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(properties.getStillTexture(stack));
            renderFluid(graphics, sprite, color, x, y, width, height);
        }
    }

    public static void renderFluid(GuiGraphics graphics, int color, int x, int y, int width, int height) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(IClientFluidTypeExtensions.of(Fluids.WATER).getStillTexture());
        renderFluid(graphics, sprite, color, x, y, width, height);
    }

    private static void renderFluid(GuiGraphics graphics, TextureAtlasSprite sprite, int color, int x, int y, int width, int height) {
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 100);
        // Some mods set alpha, other leave it 0, so we use the alpha whenever it is not 0.
        int alpha = (color >>> 24) & 0xFF;
        if (alpha > 0) {
            RenderHelper.argb(color);
            if (alpha < 255) {
                RenderSystem.enableBlend();
            }
        } else {
            RenderHelper.rgb(color);
        }
        RenderHelper.repeatBlit(graphics, x, y, width, height, sprite);
        if (alpha > 0 && alpha < 255) {
            RenderSystem.disableBlend();
        }
        RenderHelper.resetColor();
        graphics.pose().popPose();
    }
}

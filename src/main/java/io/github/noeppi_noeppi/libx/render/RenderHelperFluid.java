package io.github.noeppi_noeppi.libx.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.fluids.FluidStack;

/**
 * Helper class to render fluids into a gui. This can either render {@link FluidStack fluid stacks} or {@link Fluid fluids} with a special color.
 */
public class RenderHelperFluid {

    public static void renderFluid(PoseStack poseStack, MultiBufferSource buffer, FluidStack stack, int x, int y, int width, int height) {
        if (!stack.isEmpty()) {
            Fluid fluid = stack.getFluid();
            int color = fluid.getAttributes().getColor(stack);
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluid.getAttributes().getStillTexture(stack));
            renderFluid(poseStack, buffer, sprite, color, x, y, width, height);
        }
    }

    public static void renderFluid(PoseStack poseStack, MultiBufferSource buffer, int color, int x, int y, int width, int height) {
        Fluid fluid = Fluids.WATER;
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluid.getAttributes().getStillTexture());
        renderFluid(poseStack, buffer, sprite, color, x, y, width, height);
    }

    private static void renderFluid(PoseStack poseStack, MultiBufferSource buffer, TextureAtlasSprite sprite, int color, int x, int y, int width, int height) {
        poseStack.pushPose();
        poseStack.translate(0, 0, 100);
        Minecraft.getInstance().getTextureManager().bind(sprite.atlas().location());
        RenderHelper.color(color);
        RenderHelper.repeatBlit(poseStack, x, y, width, height, sprite);
        RenderHelper.resetColor();
        poseStack.popPose();
    }
}

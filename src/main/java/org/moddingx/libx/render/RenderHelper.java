package org.moddingx.libx.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.moddingx.libx.LibX;

/**
 * Some utilities for rendering in general.
 */
public class RenderHelper {

    /**
     * ResourceLocation of a texture with the size 512x512 that is purely white. It can be colored with
     * {@link RenderSystem#setShaderColor(float, float, float, float)}.
     */
    public static final ResourceLocation TEXTURE_WHITE = LibX.getInstance().resource("textures/white.png");
    public static final ResourceLocation TEXTURE_CHEST_GUI = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");

    /**
     * Same as {@link #repeatBlit(GuiGraphics, int, int, int, int, int, int, TextureAtlasSprite)}. texWidth and texHeight are set from the sprite.
     */
    public static void repeatBlit(GuiGraphics graphics, int x, int y, int displayWidth, int displayHeight, TextureAtlasSprite sprite) {
        repeatBlit(graphics, sprite.atlasLocation(), x, y, sprite.contents().width(), sprite.contents().height(), displayWidth, displayHeight, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1());
    }

    /**
     * Repeatedly blits a texture
     *
     * @param x             x coordinate of top-left corner
     * @param y             y coordinate of top-left corner
     * @param texWidth      width of one texture element. If this is lower than displayWidth the texture is looped.
     * @param texHeight     height of one texture element. If this is lower than displayHeight the texture is looped.
     * @param displayWidth  the width of the blit
     * @param displayHeight the height of the blit
     * @param sprite        A texture sprite
     */
    public static void repeatBlit(GuiGraphics graphics, int x, int y, int texWidth, int texHeight, int displayWidth, int displayHeight, TextureAtlasSprite sprite) {
        repeatBlit(graphics, sprite.atlasLocation(), x, y, texWidth, texHeight, displayWidth, displayHeight, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1());
    }

    /**
     * Same as {@link #repeatBlit(GuiGraphics, int, int, int, int, int, int, TextureAtlasSprite)} but with the u and v values set directly and not with a TextureAtlasSprite.
     */
    public static void repeatBlit(GuiGraphics graphics, ResourceLocation texture, int x, int y, int texWidth, int texHeight, int displayWidth, int displayHeight, float minU, float maxU, float minV, float maxV) {
        int pixelsRenderedX = 0;
        while (pixelsRenderedX < displayWidth) {
            int pixelsNowX = Math.min(texWidth, displayWidth - pixelsRenderedX);
            float maxUThisTime = maxU;
            if (pixelsNowX < texWidth) {
                maxUThisTime = minU + ((maxU - minU) * (pixelsNowX / (float) texWidth));
            }

            int pixelsRenderedY = 0;
            while (pixelsRenderedY < displayHeight) {
                int pixelsNowY = Math.min(texHeight, displayHeight - pixelsRenderedY);
                float maxVThisTime = maxV;
                if (pixelsNowY < texHeight) {
                    maxVThisTime = minV + ((maxV - minV) * (pixelsNowY / (float) texHeight));
                }

                graphics.innerBlit(texture, x + pixelsRenderedX, x + pixelsRenderedX + pixelsNowX,
                        y + pixelsRenderedY, y + pixelsRenderedY + pixelsNowY,
                        0, minU, maxUThisTime, minV, maxVThisTime);

                pixelsRenderedY += pixelsNowY;
            }
            pixelsRenderedX += pixelsNowX;
        }
    }

    /**
     * Renders a texture colored with a given color.
     *
     * @param buffer  A VertexBuilder used to render the sprite.
     * @param x       x coordinate of top-left corner
     * @param y       y coordinate of top-left corner
     * @param sprite  A texture sprite
     * @param width   The width of the icon
     * @param height  The height of the icon
     * @param alpha   The alpha value that should be used.
     * @param color   A color in format 0xRRGGBB
     * @param light   Light value
     * @param overlay Value on the overlay map.
     */
    public static void renderIconColored(PoseStack poseStack, VertexConsumer buffer, float x, float y, TextureAtlasSprite sprite, float width, float height, float alpha, int color, int light, int overlay) {
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;
        Matrix4f pose = poseStack.last().pose();
        buffer.vertex(pose, x, y + height, 0.0F).color(red, green, blue, (int) (alpha * 255.0F)).uv(sprite.getU0(), sprite.getV1()).overlayCoords(overlay).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
        buffer.vertex(pose, x + width, y + height, 0.0F).color(red, green, blue, (int) (alpha * 255.0F)).uv(sprite.getU1(), sprite.getV1()).overlayCoords(overlay).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
        buffer.vertex(pose, x + width, y, 0.0F).color(red, green, blue, (int) (alpha * 255.0F)).uv(sprite.getU1(), sprite.getV0()).overlayCoords(overlay).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
        buffer.vertex(pose, x, y, 0.0F).color(red, green, blue, (int) (alpha * 255.0F)).uv(sprite.getU0(), sprite.getV0()).overlayCoords(overlay).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
    }

    /**
     * Sets the color to the given RGB color in format 0xRRGGBB
     */
    public static void rgb(int color) {
        RenderSystem.setShaderColor(((color >>> 16) & 0xFF) / 255f, ((color >>> 8) & 0xFF) / 255f, (color & 0xFF) / 255f, 1);
    }

    /**
     * Sets the color to the given ARGB color in format 0xAARRGGBB
     */
    public static void argb(int color) {
        RenderSystem.setShaderColor(((color >>> 16) & 0xFF) / 255f, ((color >>> 8) & 0xFF) / 255f, (color & 0xFF) / 255f, ((color >>> 24) & 0xFF) / 255f);
    }

    /**
     * Resets the color to white.
     */
    public static void resetColor() {
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    /**
     * Same as {@link #renderGuiBackground(GuiGraphics, int, int, int, int, ResourceLocation, int, int, int, int, int, int) renderGuiBackground} but with pre-set texture.
     */
    public static void renderGuiBackground(GuiGraphics graphics, int x, int y, int width, int height) {
        renderGuiBackground(graphics, x, y, width, height, TEXTURE_CHEST_GUI);
    }

    /**
     * Same as {@link #renderGuiBackground(GuiGraphics, int, int, int, int, ResourceLocation, int, int, int, int, int, int) renderGuiBackground}
     * with the default GUI texture.
     */
    public static void renderGuiBackground(GuiGraphics graphics, int x, int y, int width, int height, ResourceLocation texture) {
        renderGuiBackground(graphics, x, y, width, height, texture, 176, 222, 7, 169, 125, 139);
    }

    /**
     * Renders a gui background of any size. This is created from the chest GUI texture and should
     * work with texture packs. The width and height must be at least 9.
     *
     * @param x        The x position for the top left corner.
     * @param y        The y position for the top left corner.
     * @param width    The width of the GUI background
     * @param height   The height of the GUI background
     * @param texture  The texture to use for the GUI background
     * @param textureX Texture size x
     * @param textureY Texture size y
     * @param minU     The minimum u position for the filling part of the texture
     * @param maxU     The maximum u position for the filling part of the texture
     * @param minV     The minimum v position for the filling part of the texture
     * @param maxV     The maximum v position for the filling part of the texture
     */
    public static void renderGuiBackground(GuiGraphics graphics, int x, int y, int width, int height, ResourceLocation texture, int textureX, int textureY, int minU, int maxU, int minV, int maxV) {
        // Background
        repeatBlit(graphics, texture, x + 2, y + 2,
                maxU - minU, maxV - minV, width - 4, height - 4,
                minU / 256f, maxU / 256f, minV / 256f, maxV / 256f);
        // Corners
        graphics.blit(texture, x, y, 0, 0, 0, 4, 4, 256, 256);
        graphics.blit(texture, x + width - 5, y, 0, textureX - 4, 0, 4, 4, 256, 256);
        graphics.blit(texture, x, y + height - 5, 0, 0, textureY - 4, 4, 4, 256, 256);
        graphics.blit(texture, x + width - 5, y + height - 5, 0, textureX - 4, textureY - 4, 4, 4, 256, 256);
        // Top edge
        repeatBlit(graphics, texture, x + 4, y,
                169, 3, width - 8, 3,
                4 / 256f, (textureX - 3) / 256f, 0 / 256f, 3 / 256f);
        // Bottom edge
        repeatBlit(graphics, texture, x + 4, y + height - 4,
                169, 3, width - 8, 3,
                4 / 256f, (textureX - 3) / 256f, (textureY - 3) / 256f, textureY / 256f);
        // Left edge
        repeatBlit(graphics, texture, x, y + 4,
                3, 214, 3, height - 8,
                0 / 256f, 3 / 256f, 4 / 256f, (textureY - 4) / 256f);
        // Right edge
        repeatBlit(graphics, texture, x + width - 4, y + 4,
                3, 214, 3, height - 8,
                (textureX - 3) / 256f, textureX / 256f, 4 / 256f, (textureY - 4) / 256f);
    }
}

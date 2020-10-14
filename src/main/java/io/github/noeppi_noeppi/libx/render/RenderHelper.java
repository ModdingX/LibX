package io.github.noeppi_noeppi.libx.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.noeppi_noeppi.libx.LibX;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.GL11;

/**
 * Some utilities for rendering in general.
 */
public class RenderHelper {

    /**
     * ResourceLocation of a texture with the size 512x512 that is purely white and because of this can
     * be colored via {@link RenderHelper#color(int)}
     */
    public static final ResourceLocation TEXTURE_WHITE = new ResourceLocation(LibX.getInstance().modid, "textures/white.png");

    /**
     * Same as {@link RenderHelper#repeatBlit(MatrixStack, int, int, int, int, int, int, TextureAtlasSprite)}. texWidth and texHeight are set from the sprite.
     */
    public static void repeatBlit(MatrixStack matrixStack, int x, int y, int displayWidth, int displayHeight, TextureAtlasSprite sprite) {
        repeatBlit(matrixStack, x, y, sprite.getWidth(), sprite.getHeight(), displayWidth, displayHeight, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
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
    public static void repeatBlit(MatrixStack ms, int x, int y, int texWidth, int texHeight, int displayWidth, int displayHeight, TextureAtlasSprite sprite) {
        repeatBlit(ms, x, y, texWidth, texHeight, displayWidth, displayHeight, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
    }

    /**
     * Same as {@link RenderHelper#repeatBlit(MatrixStack, int, int, int, int, int, int, TextureAtlasSprite)} but without the u and v values set directly and not with a TextureAtlasSprite.
     */
    public static void repeatBlit(MatrixStack ms, int x, int y, int texWidth, int texHeight, int displayWidth, int displayHeight, float minU, float maxU, float minV, float maxV) {
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

                AbstractGui.innerBlit(ms.getLast().getMatrix(), x + pixelsRenderedX, x + pixelsRenderedX + pixelsNowX,
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
    public static void renderIconColored(MatrixStack matrixStack, IVertexBuilder buffer, float x, float y, TextureAtlasSprite sprite, float width, float height, float alpha, int color, int light, int overlay) {
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;
        Matrix4f mat = matrixStack.getLast().getMatrix();
        buffer.pos(mat, x, y + height, 0.0F).color(red, green, blue, (int) (alpha * 255.0F)).tex(sprite.getMinU(), sprite.getMaxV()).overlay(overlay).lightmap(light).normal(0.0F, 0.0F, 1.0F).endVertex();
        buffer.pos(mat, x + width, y + height, 0.0F).color(red, green, blue, (int) (alpha * 255.0F)).tex(sprite.getMaxU(), sprite.getMaxV()).overlay(overlay).lightmap(light).normal(0.0F, 0.0F, 1.0F).endVertex();
        buffer.pos(mat, x + width, y, 0.0F).color(red, green, blue, (int) (alpha * 255.0F)).tex(sprite.getMaxU(), sprite.getMinV()).overlay(overlay).lightmap(light).normal(0.0F, 0.0F, 1.0F).endVertex();
        buffer.pos(mat, x, y, 0.0F).color(red, green, blue, (int) (alpha * 255.0F)).tex(sprite.getMinU(), sprite.getMinV()).overlay(overlay).lightmap(light).normal(0.0F, 0.0F, 1.0F).endVertex();
    }

    /**
     * Sets the color to the given RGB color in format 0xRRGGBB
     */
    public static void color(int color) {
        //noinspection deprecation
        RenderSystem.color3f(((color >>> 16) & 0xFF) / 255f, ((color >>> 8) & 0xFF) / 255f, (color & 0xFF) / 255f);
    }

    /**
     * Resets the color to white.
     */
    public static void resetColor() {
        //noinspection deprecation
        RenderSystem.color3f(1, 1, 1);
    }

    /**
     * Renders a text with a gray semi-transparent background.
     */
    public static void renderText(String text, MatrixStack matrixStack, IRenderTypeBuffer buffer) {
        float widthHalf = Minecraft.getInstance().fontRenderer.getStringWidth(text) / 2f;
        float heightHalf = Minecraft.getInstance().fontRenderer.FONT_HEIGHT / 2f;

        matrixStack.push();
        matrixStack.translate(-(widthHalf + 2), -(heightHalf + 2), 0);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        //noinspection deprecation
        GlStateManager.color4f(0.2f, 0.2f, 0.2f, 0.8f);
        Minecraft.getInstance().getTextureManager().bindTexture(TEXTURE_WHITE);

        AbstractGui.blit(matrixStack, 0, 0, 0, 0, (int) (2 * widthHalf) + 4, (int) (2 * heightHalf) + 4, 256, 256);

        //noinspection deprecation
        GlStateManager.color4f(1, 1, 1, 1);
        RenderSystem.disableBlend();
        matrixStack.translate(widthHalf + 2, heightHalf + 2, 10);

        Minecraft.getInstance().fontRenderer.drawString(matrixStack, text, -widthHalf, -heightHalf, 0xFFFFFF);
        matrixStack.pop();
    }
}

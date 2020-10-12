package io.github.noeppi_noeppi.libx.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.noeppi_noeppi.libx.LibX;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;

public class RenderHelper {

    public static final ResourceLocation TEXTURE_WHITE = new ResourceLocation(LibX.getInstance().modid, "textures/white.png");

    public static void repeatBlit(MatrixStack matrixStack, int x, int y, int displayWidth, int displayHeight, TextureAtlasSprite sprite) {
        repeatBlit(matrixStack, x, y, sprite.getWidth(), sprite.getHeight(), displayWidth, displayHeight, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
    }

    /**
     * Repeatedly blits a texture
     *
     * @param x             x coordinate of topleft corner
     * @param y             y coordinate of topleft corner
     * @param texWidth      width of one texture element. If this is lower than displayWidth the texture is looped.
     * @param texHeight     height of one texture element. If this is lower than displayHeight the texture is looped.
     * @param displayWidth  the width of the blit
     * @param displayHeight the height of the blit
     * @param sprite        A texture sprite
     */
    public static void repeatBlit(MatrixStack ms, int x, int y, int texWidth, int texHeight, int displayWidth, int displayHeight, TextureAtlasSprite sprite) {
        repeatBlit(ms, x, y, texWidth, texHeight, displayWidth, displayHeight, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
    }

    public static void repeatBlit(MatrixStack ms, int x, int y, int texWidth, int texHeight, int displayWidth, int displayHeight, float minU, float maxU, float minV, float maxV) {
        int pixelsRenderedX = 0;
        while (pixelsRenderedX < displayWidth) {
            int pixelsNowX = Math.min(texWidth, displayWidth - pixelsRenderedX);
            float maxUnow = maxU;
            if (pixelsNowX < texWidth) {
                maxUnow = minU + ((maxU - minU) * (pixelsNowX / (float) texWidth));
            }

            int pixelsRenderedY = 0;
            while (pixelsRenderedY < displayHeight) {
                int pixelsNowY = Math.min(texHeight, displayHeight - pixelsRenderedY);
                float maxVnow = maxV;
                if (pixelsNowY < texHeight) {
                    maxVnow = minV + ((maxV - minV) * (pixelsNowY / (float) texHeight));
                }

                AbstractGui.innerBlit(ms.getLast().getMatrix(), x + pixelsRenderedX, x + pixelsRenderedX + pixelsNowX,
                        y + pixelsRenderedY, y + pixelsRenderedY + pixelsNowY,
                        0, minU, maxUnow, minV, maxVnow);

                pixelsRenderedY += pixelsNowY;
            }
            pixelsRenderedX += pixelsNowX;
        }
    }

    public static void renderIconColored(MatrixStack matrixStack, IVertexBuilder buffer, float x, float y, TextureAtlasSprite sprite, float width, float height, float alpha, int color, int light, int overlay) {
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;
        Matrix4f mat = matrixStack.getLast().getMatrix();
        buffer.pos(mat, x, y + height, 0.0F).color(red, green, blue, (int)(alpha * 255.0F)).tex(sprite.getMinU(), sprite.getMaxV()).overlay(overlay).lightmap(light).normal(0.0F, 0.0F, 1.0F).endVertex();
        buffer.pos(mat, x + width, y + height, 0.0F).color(red, green, blue, (int)(alpha * 255.0F)).tex(sprite.getMaxU(), sprite.getMaxV()).overlay(overlay).lightmap(light).normal(0.0F, 0.0F, 1.0F).endVertex();
        buffer.pos(mat, x + width, y, 0.0F).color(red, green, blue, (int)(alpha * 255.0F)).tex(sprite.getMaxU(), sprite.getMinV()).overlay(overlay).lightmap(light).normal(0.0F, 0.0F, 1.0F).endVertex();
        buffer.pos(mat, x, y, 0.0F).color(red, green, blue, (int)(alpha * 255.0F)).tex(sprite.getMinU(), sprite.getMinV()).overlay(overlay).lightmap(light).normal(0.0F, 0.0F, 1.0F).endVertex();
    }

    public static void color(int color) {
        RenderSystem.color3f(((color >>> 16) & 0xFF) / 255f, ((color >>> 8) & 0xFF) / 255f, (color & 0xFF) / 255f);
    }

    public static void resetColor() {
        RenderSystem.color3f(1, 1, 1);
    }
}

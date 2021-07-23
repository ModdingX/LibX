package io.github.noeppi_noeppi.libx.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.noeppi_noeppi.libx.LibX;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.core.Vec3i;
import com.mojang.math.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Some utilities for rendering in general.
 */
public class RenderHelper {

    /**
     * ResourceLocation of a texture with the size 512x512 that is purely white and because of this can
     * be colored via {@link RenderHelper#color(int)}
     */
    public static final ResourceLocation TEXTURE_WHITE = new ResourceLocation(LibX.getInstance().modid, "textures/white.png");
    private static final ResourceLocation TEXTURE_CHEST_GUI = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");

    /**
     * Same as {@link RenderHelper#repeatBlit(MatrixStack, int, int, int, int, int, int, TextureAtlasSprite)}. texWidth and texHeight are set from the sprite.
     */
    public static void repeatBlit(PoseStack poseStack, int x, int y, int displayWidth, int displayHeight, TextureAtlasSprite sprite) {
        repeatBlit(poseStack, x, y, sprite.getWidth(), sprite.getHeight(), displayWidth, displayHeight, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1());
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
    public static void repeatBlit(PoseStack ms, int x, int y, int texWidth, int texHeight, int displayWidth, int displayHeight, TextureAtlasSprite sprite) {
        repeatBlit(ms, x, y, texWidth, texHeight, displayWidth, displayHeight, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1());
    }

    /**
     * Same as {@link RenderHelper#repeatBlit(MatrixStack, int, int, int, int, int, int, TextureAtlasSprite)} but with the u and v values set directly and not with a TextureAtlasSprite.
     */
    public static void repeatBlit(PoseStack ms, int x, int y, int texWidth, int texHeight, int displayWidth, int displayHeight, float minU, float maxU, float minV, float maxV) {
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

                GuiComponent.innerBlit(ms.last().pose(), x + pixelsRenderedX, x + pixelsRenderedX + pixelsNowX,
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
        Matrix4f mat = poseStack.last().pose();
        buffer.vertex(mat, x, y + height, 0.0F).color(red, green, blue, (int) (alpha * 255.0F)).uv(sprite.getU0(), sprite.getV1()).overlayCoords(overlay).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
        buffer.vertex(mat, x + width, y + height, 0.0F).color(red, green, blue, (int) (alpha * 255.0F)).uv(sprite.getU1(), sprite.getV1()).overlayCoords(overlay).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
        buffer.vertex(mat, x + width, y, 0.0F).color(red, green, blue, (int) (alpha * 255.0F)).uv(sprite.getU1(), sprite.getV0()).overlayCoords(overlay).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
        buffer.vertex(mat, x, y, 0.0F).color(red, green, blue, (int) (alpha * 255.0F)).uv(sprite.getU0(), sprite.getV0()).overlayCoords(overlay).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
    }

    /**
     * Sets the color to the given RGB color in format 0xRRGGBB
     */
    public static void color(int color) {
        // FIXME find a solution for this
//        //noinspection deprecation
//        RenderSystem.color3f(((color >>> 16) & 0xFF) / 255f, ((color >>> 8) & 0xFF) / 255f, (color & 0xFF) / 255f);
    }

    /**
     * Resets the color to white.
     */
    public static void resetColor() {
        // FIXME find a solution for this
//        //noinspection deprecation
//        RenderSystem.color3f(1, 1, 1);
    }

    /**
     * Renders a text with a gray semi-transparent background.
     */
    public static void renderText(String text, PoseStack poseStack, MultiBufferSource buffer) {
        float widthHalf = Minecraft.getInstance().font.width(text) / 2f;
        float heightHalf = Minecraft.getInstance().font.lineHeight / 2f;

        poseStack.pushPose();
        poseStack.translate(-(widthHalf + 2), -(heightHalf + 2), 0);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        // FIXME find a solution for this
//        //noinspection deprecation
//        GlStateManager._color4f(0.2f, 0.2f, 0.2f, 0.8f);
        Minecraft.getInstance().getTextureManager().bindForSetup(TEXTURE_WHITE);

        GuiComponent.blit(poseStack, 0, 0, 0, 0, (int) (2 * widthHalf) + 4, (int) (2 * heightHalf) + 4, 256, 256);

        // FIXME find a solution for this
//        //noinspection deprecation
//        GlStateManager._color4f(1, 1, 1, 1);
        RenderSystem.disableBlend();
        poseStack.translate(widthHalf + 2, heightHalf + 2, 10);

        Minecraft.getInstance().font.draw(poseStack, text, -widthHalf, -heightHalf, 0xFFFFFF);
        poseStack.popPose();
    }

    /**
     * Works like {@link IVertexBuilder#addQuad} but allows you to modify alpha values as well. Like
     * {@link IVertexBuilder#addQuad} this uses {@link DefaultVertexFormats#BLOCK}.
     *
     * @param alpha    The alpha value to use.
     * @param mulAlpha If set to true the given alpha value is multiplied with the value set in
     *                 the four byte of {@code COLOR_4UB} assuming it is stored as {@code RGBA}.
     *                 If set to false just the given alpha value will be used.
     */
    public static void addQuadWithAlpha(VertexConsumer vertex, PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, float alpha, int light, int overlay, boolean mulColor, boolean mulAlpha) {
        int[] vertexData = quad.getVertices();
        Vec3i vector3i = quad.getDirection().getNormal();
        Vector3f vector3f = new Vector3f((float) vector3i.getX(), (float) vector3i.getY(), (float) vector3i.getZ());
        Matrix4f matrix4f = pose.pose();
        vector3f.transform(pose.normal());

        try (MemoryStack memorystack = MemoryStack.stackPush()) {
            ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
            IntBuffer intbuffer = bytebuffer.asIntBuffer();

            for (int i = 0; i < vertexData.length / 8; i++) {
                intbuffer.clear();
                intbuffer.put(vertexData, i * 8, 8);
                float x = bytebuffer.getFloat(0);
                float y = bytebuffer.getFloat(4);
                float z = bytebuffer.getFloat(8);
                float a;
                float r;
                float g;
                float b;

                if (mulAlpha) {
                    a = (float) (bytebuffer.get(15) & 255) / 255.0F * alpha;
                } else {
                    a = alpha;
                }

                if (mulColor) {
                    r = (float) (bytebuffer.get(12) & 255) / 255.0F * red;
                    g = (float) (bytebuffer.get(13) & 255) / 255.0F * green;
                    b = (float) (bytebuffer.get(14) & 255) / 255.0F * blue;
                } else {
                    r = red;
                    g = green;
                    b = blue;
                }

                int l = vertex.applyBakedLighting(light, bytebuffer);
                float u = bytebuffer.getFloat(16);
                float v = bytebuffer.getFloat(20);
                Vector4f vector4f = new Vector4f(x, y, z, 1.0F);
                vector4f.transform(matrix4f);
                vertex.applyBakedNormals(vector3f, bytebuffer, pose.normal());
                vertex.vertex(vector4f.x(), vector4f.y(), vector4f.z(), r, g, b, a, u, v, overlay, l, vector3f.x(), vector3f.y(), vector3f.z());
            }
        }
    }

    /**
     * Renders a gui background of any size. This is created from the chest GUI texture and should
     * work with texture packs. The width and height must be at least 9.
     *
     * @param x      The x position for the top left corner.
     * @param y      The y position for the top left corner.
     * @param width  The width of the GUI background
     * @param height The height of the GUI background
     */
    public static void renderGuiBackground(PoseStack poseStack, int x, int y, int width, int height) {
        // FIXME find a solution for this
//        //noinspection deprecation
//        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getInstance().getTextureManager().bindForSetup(TEXTURE_CHEST_GUI);
        // Background
        repeatBlit(poseStack, x + 2, y + 2,
                162, 14, width - 4, height - 4,
                7 / 256f, 169 / 256f, 125 / 256f, 139 / 256f);
        // Corners
        GuiComponent.blit(poseStack, x, y, 0, 0, 0, 4, 4, 256, 256);
        GuiComponent.blit(poseStack, x + width - 5, y, 0, 172, 0, 4, 4, 256, 256);
        GuiComponent.blit(poseStack, x, y + height - 5, 0, 0, 218, 4, 4, 256, 256);
        GuiComponent.blit(poseStack, x + width - 5, y + height - 5, 0, 172, 218, 4, 4, 256, 256);
        // Top edge
        repeatBlit(poseStack, x + 4, y,
                169, 3, width - 8, 3,
                4 / 256f, 173 / 256f, 0 / 256f, 3 / 256f);
        // Bottom edge
        repeatBlit(poseStack, x + 4, y + height - 4,
                169, 3, width - 8, 3,
                4 / 256f, 173 / 256f, 219 / 256f, 222 / 256f);
        // Left edge
        repeatBlit(poseStack, x, y + 4,
                3, 214, 3, height - 8,
                0 / 256f, 3 / 256f, 4 / 256f, 218 / 256f);
        // Right edge
        repeatBlit(poseStack, x + width - 4, y + 4,
                3, 214, 3, height - 8,
                173 / 256f, 176 / 256f, 4 / 256f, 218 / 256f);
    }
}

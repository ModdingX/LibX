package org.moddingx.libx.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * A {@link GuiGraphics} that overrides all methods and passes them through to the given parent. Useful if you
 * need to change the behaviour of some methods in some specific part of code.
 */
public class FilterGuiGraphics extends GuiGraphics {
    
    protected final GuiGraphics parent;
    
    public FilterGuiGraphics(GuiGraphics parent) {
        super(Minecraft.getInstance(), parent.bufferSource());
        this.parent = parent;
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public void drawManaged(@Nonnull Runnable action) {
        this.parent.drawManaged(action);
    }

    @Override
    public int guiWidth() {
        return super.guiWidth();
    }

    @Override
    public int guiHeight() {
        return super.guiHeight();
    }

    @Nonnull
    @Override
    public PoseStack pose() {
        return super.pose();
    }

    @Nonnull
    @Override
    public MultiBufferSource.BufferSource bufferSource() {
        return super.bufferSource();
    }

    @Override
    public void flush() {
        super.flush();
    }

    @Override
    public void hLine(int minX, int maxX, int y, int color) {
        super.hLine(minX, maxX, y, color);
    }

    @Override
    public void hLine(@Nonnull RenderType renderType, int minX, int maxX, int y, int color) {
        super.hLine(renderType, minX, maxX, y, color);
    }

    @Override
    public void vLine(int x, int minY, int maxY, int color) {
        super.vLine(x, minY, maxY, color);
    }

    @Override
    public void vLine(@Nonnull RenderType renderType, int x, int minY, int maxY, int color) {
        super.vLine(renderType, x, minY, maxY, color);
    }

    @Override
    public void enableScissor(int minX, int minY, int maxX, int maxY) {
        super.enableScissor(minX, minY, maxX, maxY);
    }

    @Override
    public void disableScissor() {
        super.disableScissor();
    }

    @Override
    public void setColor(float red, float green, float blue, float alpha) {
        super.setColor(red, green, blue, alpha);
    }

    @Override
    public void fill(int minX, int minY, int maxX, int maxY, int color) {
        super.fill(minX, minY, maxX, maxY, color);
    }

    @Override
    public void fill(int minX, int minY, int maxX, int maxY, int z, int color) {
        super.fill(minX, minY, maxX, maxY, z, color);
    }

    @Override
    public void fill(@Nonnull RenderType renderType, int minX, int minY, int maxX, int maxY, int color) {
        super.fill(renderType, minX, minY, maxX, maxY, color);
    }

    @Override
    public void fill(@Nonnull RenderType renderType, int minX, int minY, int maxX, int maxY, int z, int color) {
        super.fill(renderType, minX, minY, maxX, maxY, z, color);
    }

    @Override
    public void fillGradient(int x1, int y1, int x2, int y2, int colorFrom, int colorTo) {
        super.fillGradient(x1, y1, x2, y2, colorFrom, colorTo);
    }

    @Override
    public void fillGradient(int x1, int y1, int x2, int y2, int z, int colorFrom, int colorTo) {
        super.fillGradient(x1, y1, x2, y2, z, colorFrom, colorTo);
    }

    @Override
    public void fillGradient(@Nonnull RenderType renderType, int x1, int y1, int x2, int y2, int colorFrom, int colorTo, int z) {
        super.fillGradient(renderType, x1, y1, x2, y2, colorFrom, colorTo, z);
    }

    @Override
    public void drawCenteredString(@Nonnull Font font, @Nonnull String text, int x, int y, int color) {
        super.drawCenteredString(font, text, x, y, color);
    }

    @Override
    public void drawCenteredString(@Nonnull Font font, @Nonnull Component text, int x, int y, int color) {
        super.drawCenteredString(font, text, x, y, color);
    }

    @Override
    public void drawCenteredString(@Nonnull Font font, @Nonnull FormattedCharSequence text, int x, int y, int color) {
        super.drawCenteredString(font, text, x, y, color);
    }

    @Override
    public int drawString(@Nonnull Font font, @Nullable String text, int x, int y, int color) {
        return super.drawString(font, text, x, y, color);
    }

    @Override
    public int drawString(@Nonnull Font font, @Nullable String text, int x, int y, int color, boolean dropShadow) {
        return super.drawString(font, text, x, y, color, dropShadow);
    }

    @Override
    public int drawString(@Nonnull Font font, @Nullable String text, float x, float y, int color, boolean dropShadow) {
        return super.drawString(font, text, x, y, color, dropShadow);
    }

    @Override
    public int drawString(@Nonnull Font font, @Nonnull FormattedCharSequence text, int x, int y, int color) {
        return super.drawString(font, text, x, y, color);
    }

    @Override
    public int drawString(@Nonnull Font font, @Nonnull FormattedCharSequence text, int x, int y, int color, boolean dropShadow) {
        return super.drawString(font, text, x, y, color, dropShadow);
    }

    @Override
    public int drawString(@Nonnull Font font, @Nonnull FormattedCharSequence text, float x, float y, int color, boolean dropShadow) {
        return super.drawString(font, text, x, y, color, dropShadow);
    }

    @Override
    public int drawString(@Nonnull Font font, @Nonnull Component text, int x, int y, int color) {
        return super.drawString(font, text, x, y, color);
    }

    @Override
    public int drawString(@Nonnull Font font, @Nonnull Component text, int x, int y, int color, boolean dropShadow) {
        return super.drawString(font, text, x, y, color, dropShadow);
    }

    @Override
    public void drawWordWrap(@Nonnull Font font, @Nonnull FormattedText text, int x, int y, int lineWidth, int color) {
        super.drawWordWrap(font, text, x, y, lineWidth, color);
    }

    @Override
    public void blit(int x, int y, int blitOffset, int width, int height, @Nonnull TextureAtlasSprite sprite) {
        super.blit(x, y, blitOffset, width, height, sprite);
    }

    @Override
    public void blit(int x, int y, int blitOffset, int width, int height, @Nonnull TextureAtlasSprite sprite, float red, float green, float blue, float alpha) {
        super.blit(x, y, blitOffset, width, height, sprite, red, green, blue, alpha);
    }

    @Override
    public void renderOutline(int x, int y, int width, int height, int color) {
        super.renderOutline(x, y, width, height, color);
    }

    @Override
    public void blit(@Nonnull ResourceLocation atlasLocation, int x, int y, int uOffset, int vOffset, int uWidth, int vHeight) {
        super.blit(atlasLocation, x, y, uOffset, vOffset, uWidth, vHeight);
    }

    @Override
    public void blit(@Nonnull ResourceLocation atlasLocation, int x, int y, int blitOffset, float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        super.blit(atlasLocation, x, y, blitOffset, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight);
    }

    @Override
    public void blit(@Nonnull ResourceLocation atlasLocation, int x, int y, int width, int height, float uWidth, float vHeight, int uOffset, int vOffset, int textureWidth, int textureHeight) {
        super.blit(atlasLocation, x, y, width, height, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight);
    }

    @Override
    public void blit(@Nonnull ResourceLocation atlasLocation, int x, int y, float uOffset, float vOffset, int width, int height, int textureWidth, int textureHeight) {
        super.blit(atlasLocation, x, y, uOffset, vOffset, width, height, textureWidth, textureHeight);
    }

    @Override
    public void innerBlit(@Nonnull ResourceLocation atlasLocation, int x1, int x2, int y1, int y2, int blitOffset, float minU, float maxU, float minV, float maxV) {
        super.innerBlit(atlasLocation, x1, x2, y1, y2, blitOffset, minU, maxU, minV, maxV);
    }

    @Override
    public void blitNineSliced(@Nonnull ResourceLocation atlasLocation, int targetX, int targetY, int targetWidth, int targetHeight, int sliceSize, int sourceWidth, int sourceHeight, int sourceX, int sourceY) {
        super.blitNineSliced(atlasLocation, targetX, targetY, targetWidth, targetHeight, sliceSize, sourceWidth, sourceHeight, sourceX, sourceY);
    }

    @Override
    public void blitNineSliced(@Nonnull ResourceLocation atlasLocation, int targetX, int targetY, int targetWidth, int targetHeight, int sliceWidth, int sliceHeight, int sourceWidth, int sourceHeight, int sourceX, int sourceY) {
        super.blitNineSliced(atlasLocation, targetX, targetY, targetWidth, targetHeight, sliceWidth, sliceHeight, sourceWidth, sourceHeight, sourceX, sourceY);
    }

    @Override
    public void blitNineSliced(@Nonnull ResourceLocation atlasLocation, int targetX, int targetY, int targetWidth, int targetHeight, int cornerWidth, int cornerHeight, int edgeWidth, int edgeHeight, int sourceWidth, int sourceHeight, int sourceX, int sourceY) {
        super.blitNineSliced(atlasLocation, targetX, targetY, targetWidth, targetHeight, cornerWidth, cornerHeight, edgeWidth, edgeHeight, sourceWidth, sourceHeight, sourceX, sourceY);
    }

    @Override
    public void blitNineSlicedSized(@Nonnull ResourceLocation atlasLocation, int targetX, int targetY, int targetWidth, int targetHeight, int sliceSize, int uWidth, int vHeight, int uOffset, int vOffset, int textureWidth, int textureHeight) {
        super.blitNineSlicedSized(atlasLocation, targetX, targetY, targetWidth, targetHeight, sliceSize, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight);
    }

    @Override
    public void blitNineSlicedSized(@Nonnull ResourceLocation atlasLocation, int targetX, int targetY, int targetWidth, int targetHeight, int sliceWidth, int sliceHeight, int uWidth, int vHeight, int uOffset, int vOffset, int textureWidth, int textureHeight) {
        super.blitNineSlicedSized(atlasLocation, targetX, targetY, targetWidth, targetHeight, sliceWidth, sliceHeight, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight);
    }

    @Override
    public void blitNineSlicedSized(@Nonnull ResourceLocation atlasLocation, int targetX, int targetY, int targetWidth, int targetHeight, int cornerWidth, int cornerHeight, int edgeWidth, int edgeHeight, int uWidth, int vHeight, int uOffset, int vOffset, int textureWidth, int textureHeight) {
        super.blitNineSlicedSized(atlasLocation, targetX, targetY, targetWidth, targetHeight, cornerWidth, cornerHeight, edgeWidth, edgeHeight, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight);
    }

    @Override
    public void blitRepeating(@Nonnull ResourceLocation atlasLocation, int targetX, int targetY, int targetWidth, int targetHeight, int sourceX, int sourceY, int sourceWidth, int sourceHeight) {
        super.blitRepeating(atlasLocation, targetX, targetY, targetWidth, targetHeight, sourceX, sourceY, sourceWidth, sourceHeight);
    }

    @Override
    public void blitRepeating(@Nonnull ResourceLocation atlasLocation, int targetX, int targetY, int targetWidth, int targetHeight, int sourceX, int sourceY, int sourceWidth, int sourceHeight, int textureWidth, int textureHeight) {
        super.blitRepeating(atlasLocation, targetX, targetY, targetWidth, targetHeight, sourceX, sourceY, sourceWidth, sourceHeight, textureWidth, textureHeight);
    }

    @Override
    public void renderItem(@Nonnull ItemStack stack, int x, int y) {
        super.renderItem(stack, x, y);
    }

    @Override
    public void renderItem(@Nonnull ItemStack stack, int x, int y, int seed) {
        super.renderItem(stack, x, y, seed);
    }

    @Override
    public void renderItem(@Nonnull ItemStack stack, int x, int y, int seed, int zOffset) {
        super.renderItem(stack, x, y, seed, zOffset);
    }

    @Override
    public void renderFakeItem(@Nonnull ItemStack stack, int x, int y) {
        super.renderFakeItem(stack, x, y);
    }

    @Override
    public void renderItem(@Nonnull LivingEntity entity, @Nonnull ItemStack stack, int x, int y, int seed) {
        super.renderItem(entity, stack, x, y, seed);
    }

    @Override
    public void renderItemDecorations(@Nonnull Font font, @Nonnull ItemStack stack, int x, int y) {
        super.renderItemDecorations(font, stack, x, y);
    }

    @Override
    public void renderItemDecorations(@Nonnull Font font, @Nonnull ItemStack stack, int x, int y, @Nullable String text) {
        super.renderItemDecorations(font, stack, x, y, text);
    }

    @Override
    public void renderTooltip(@Nonnull Font font, @Nonnull ItemStack stack, int mouseX, int mouseY) {
        super.renderTooltip(font, stack, mouseX, mouseY);
    }

    @Override
    public void renderTooltip(@Nonnull Font font, @Nonnull List<Component> textComponents, @Nonnull Optional<TooltipComponent> tooltipComponent, @Nonnull ItemStack stack, int mouseX, int mouseY) {
        super.renderTooltip(font, textComponents, tooltipComponent, stack, mouseX, mouseY);
    }

    @Override
    public void renderTooltip(@Nonnull Font font, @Nonnull List<Component> tooltipLines, @Nonnull Optional<TooltipComponent> visualTooltipComponent, int mouseX, int mouseY) {
        super.renderTooltip(font, tooltipLines, visualTooltipComponent, mouseX, mouseY);
    }

    @Override
    public void renderTooltip(@Nonnull Font font, @Nonnull Component text, int mouseX, int mouseY) {
        super.renderTooltip(font, text, mouseX, mouseY);
    }

    @Override
    public void renderComponentTooltip(@Nonnull Font font, @Nonnull List<Component> tooltipLines, int mouseX, int mouseY) {
        super.renderComponentTooltip(font, tooltipLines, mouseX, mouseY);
    }

    @Override
    public void renderComponentTooltip(@Nonnull Font font, @Nonnull List<? extends FormattedText> tooltips, int mouseX, int mouseY, @Nonnull ItemStack stack) {
        super.renderComponentTooltip(font, tooltips, mouseX, mouseY, stack);
    }

    @Override
    public void renderTooltip(@Nonnull Font font, @Nonnull List<? extends FormattedCharSequence> tooltipLines, int mouseX, int mouseY) {
        super.renderTooltip(font, tooltipLines, mouseX, mouseY);
    }

    @Override
    public void renderTooltip(@Nonnull Font font, @Nonnull List<FormattedCharSequence> tooltipLines, @Nonnull ClientTooltipPositioner tooltipPositioner, int mouseX, int mouseY) {
        super.renderTooltip(font, tooltipLines, tooltipPositioner, mouseX, mouseY);
    }

    @Override
    public void renderComponentHoverEffect(@Nonnull Font font, @Nullable Style style, int mouseX, int mouseY) {
        super.renderComponentHoverEffect(font, style, mouseX, mouseY);
    }

    @Override
    public int getColorFromFormattingCharacter(char c, boolean isLighter) {
        return super.getColorFromFormattingCharacter(c, isLighter);
    }

    @Override
    public void blitWithBorder(@Nonnull ResourceLocation texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int borderSize) {
        super.blitWithBorder(texture, x, y, u, v, width, height, textureWidth, textureHeight, borderSize);
    }

    @Override
    public void blitWithBorder(@Nonnull ResourceLocation texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int topBorder, int bottomBorder, int leftBorder, int rightBorder) {
        super.blitWithBorder(texture, x, y, u, v, width, height, textureWidth, textureHeight, topBorder, bottomBorder, leftBorder, rightBorder);
    }

    @Override
    public void blitInscribed(@Nonnull ResourceLocation texture, int x, int y, int boundsWidth, int boundsHeight, int rectWidth, int rectHeight) {
        super.blitInscribed(texture, x, y, boundsWidth, boundsHeight, rectWidth, rectHeight);
    }

    @Override
    public void blitInscribed(@Nonnull ResourceLocation texture, int x, int y, int boundsWidth, int boundsHeight, int rectWidth, int rectHeight, boolean centerX, boolean centerY) {
        super.blitInscribed(texture, x, y, boundsWidth, boundsHeight, rectWidth, rectHeight, centerX, centerY);
    }
}

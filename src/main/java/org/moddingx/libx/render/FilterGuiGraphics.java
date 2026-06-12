package org.moddingx.libx.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.profiling.ResultField;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.joml.Matrix3x2fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A {@link GuiGraphics} that overrides all methods and passes them through to the given parent. Useful if you
 * need to change the behaviour of some methods in some specific part of code.
 */
public class FilterGuiGraphics extends GuiGraphics {

    protected final GuiGraphics parent;

    public FilterGuiGraphics(GuiGraphics parent) {
        super(Minecraft.getInstance(), parent.guiRenderState, parent.mouseX, parent.mouseY);
        this.parent = parent;
    }

    @Override
    public void requestCursor(@Nonnull CursorType cursor) {
        this.parent.requestCursor(cursor);
    }

    @Override
    public void applyCursor(@Nonnull Window window) {
        this.parent.applyCursor(window);
    }

    @Override
    public int guiWidth() {
        return this.parent.guiWidth();
    }

    @Override
    public int guiHeight() {
        return this.parent.guiHeight();
    }

    @Override
    public void nextStratum() {
        this.parent.nextStratum();
    }

    @Override
    public void blurBeforeThisStratum() {
        this.parent.blurBeforeThisStratum();
    }

    @Nonnull
    @Override
    public Matrix3x2fStack pose() {
        return this.parent.pose();
    }

    @Override
    public void hLine(int minX, int maxX, int y, int color) {
        this.parent.hLine(minX, maxX, y, color);
    }

    @Override
    public void vLine(int x, int minY, int maxY, int color) {
        this.parent.vLine(x, minY, maxY, color);
    }

    @Override
    public void enableScissor(int minX, int minY, int maxX, int maxY) {
        this.parent.enableScissor(minX, minY, maxX, maxY);
    }

    @Override
    public void disableScissor() {
        this.parent.disableScissor();
    }

    @Override
    public boolean containsPointInScissor(int x, int y) {
        return this.parent.containsPointInScissor(x, y);
    }

    @Override
    public void fill(int minX, int minY, int maxX, int maxY, int color) {
        this.parent.fill(minX, minY, maxX, maxY, color);
    }

    @Override
    public void fill(@Nonnull RenderPipeline pipeline, int minX, int minY, int maxX, int maxY, int color) {
        this.parent.fill(pipeline, minX, minY, maxX, maxY, color);
    }

    @Override
    public void fillGradient(int minX, int minY, int maxX, int maxY, int colorFrom, int colorTo) {
        this.parent.fillGradient(minX, minY, maxX, maxY, colorFrom, colorTo);
    }

    @Override
    public void fill(@Nonnull RenderPipeline pipeline, @Nonnull TextureSetup textureSetup, int minX, int minY, int maxX, int maxY) {
        this.parent.fill(pipeline, textureSetup, minX, minY, maxX, maxY);
    }

    @Override
    public void textHighlight(int minX, int minY, int maxX, int maxY, boolean invert) {
        this.parent.textHighlight(minX, minY, maxX, maxY, invert);
    }

    @Override
    public void drawCenteredString(@Nonnull Font font, @Nonnull String text, int x, int y, int color) {
        this.parent.drawCenteredString(font, text, x, y, color);
    }

    @Override
    public void drawCenteredString(@Nonnull Font font, @Nonnull Component text, int x, int y, int color) {
        this.parent.drawCenteredString(font, text, x, y, color);
    }

    @Override
    public void drawCenteredString(@Nonnull Font font, @Nonnull FormattedCharSequence text, int x, int y, int color) {
        this.parent.drawCenteredString(font, text, x, y, color);
    }

    @Override
    public void drawString(@Nonnull Font font, @Nullable String text, int x, int y, int color) {
        this.parent.drawString(font, text, x, y, color);
    }

    @Override
    public void drawString(@Nonnull Font font, @Nullable String text, int x, int y, int color, boolean drawShadow) {
        this.parent.drawString(font, text, x, y, color, drawShadow);
    }

    @Override
    public void drawString(@Nonnull Font font, @Nonnull FormattedCharSequence text, int x, int y, int color) {
        this.parent.drawString(font, text, x, y, color);
    }

    @Override
    public void drawString(@Nonnull Font font, @Nonnull FormattedCharSequence text, int x, int y, int color, boolean drawShadow) {
        this.parent.drawString(font, text, x, y, color, drawShadow);
    }

    @Override
    public void drawString(@Nonnull Font font, @Nonnull Component text, int x, int y, int color) {
        this.parent.drawString(font, text, x, y, color);
    }

    @Override
    public void drawString(@Nonnull Font font, @Nonnull Component text, int x, int y, int color, boolean drawShadow) {
        this.parent.drawString(font, text, x, y, color, drawShadow);
    }

    @Override
    public void drawWordWrap(@Nonnull Font font, @Nonnull FormattedText text, int x, int y, int lineWidth, int color) {
        this.parent.drawWordWrap(font, text, x, y, lineWidth, color);
    }

    @Override
    public void drawWordWrap(@Nonnull Font font, @Nonnull FormattedText text, int x, int y, int lineWidth, int color, boolean dropShadow) {
        this.parent.drawWordWrap(font, text, x, y, lineWidth, color, dropShadow);
    }

    @Override
    public void drawStringWithBackdrop(@Nonnull Font font, @Nonnull Component text, int x, int y, int width, int color) {
        this.parent.drawStringWithBackdrop(font, text, x, y, width, color);
    }

    @Override
    public void renderOutline(int x, int y, int width, int height, int color) {
        this.parent.renderOutline(x, y, width, height, color);
    }

    @Override
    public void blitSprite(@Nonnull RenderPipeline pipeline, @Nonnull Identifier sprite, int x, int y, int width, int height) {
        this.parent.blitSprite(pipeline, sprite, x, y, width, height);
    }

    @Override
    public void blitSprite(@Nonnull RenderPipeline pipeline, @Nonnull Identifier sprite, int x, int y, int width, int height, float fade) {
        this.parent.blitSprite(pipeline, sprite, x, y, width, height, fade);
    }

    @Override
    public void blitSprite(@Nonnull RenderPipeline pipeline, @Nonnull Identifier sprite, int x, int y, int width, int height, int color) {
        this.parent.blitSprite(pipeline, sprite, x, y, width, height, color);
    }

    @Override
    public void blitSprite(@Nonnull RenderPipeline pipeline, @Nonnull Identifier sprite, int textureWidth, int textureHeight, int u, int v, int x, int y, int width, int height) {
        this.parent.blitSprite(pipeline, sprite, textureWidth, textureHeight, u, v, x, y, width, height);
    }

    @Override
    public void blitSprite(@Nonnull RenderPipeline pipeline, @Nonnull Identifier sprite, int textureWidth, int textureHeight, int u, int v, int x, int y, int width, int height, int color) {
        this.parent.blitSprite(pipeline, sprite, textureWidth, textureHeight, u, v, x, y, width, height, color);
    }

    @Override
    public void blitSprite(@Nonnull RenderPipeline pipeline, @Nonnull TextureAtlasSprite sprite, int x, int width, int y, int height) {
        this.parent.blitSprite(pipeline, sprite, x, width, y, height);
    }

    @Override
    public void blitSprite(@Nonnull RenderPipeline pipeline, @Nonnull TextureAtlasSprite sprite, int x, int y, int width, int height, int color) {
        this.parent.blitSprite(pipeline, sprite, x, y, width, height, color);
    }

    @Override
    public void blit(@Nonnull RenderPipeline pipeline, @Nonnull Identifier atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color) {
        this.parent.blit(pipeline, atlas, x, y, u, v, width, height, textureWidth, textureHeight, color);
    }

    @Override
    public void blit(@Nonnull RenderPipeline pipeline, @Nonnull Identifier atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        this.parent.blit(pipeline, atlas, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    @Override
    public void blit(@Nonnull RenderPipeline pipeline, @Nonnull Identifier atlas, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        this.parent.blit(pipeline, atlas, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight);
    }

    @Override
    public void blit(@Nonnull RenderPipeline pipeline, @Nonnull Identifier atlas, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color) {
        this.parent.blit(pipeline, atlas, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, color);
    }

    @Override
    public void blit(@Nonnull Identifier atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1) {
        this.parent.blit(atlas, x0, y0, x1, y1, u0, u1, v0, v1);
    }

    @Override
    public void renderItem(@Nonnull ItemStack stack, int x, int y) {
        this.parent.renderItem(stack, x, y);
    }

    @Override
    public void renderItem(@Nonnull ItemStack stack, int x, int y, int seed) {
        this.parent.renderItem(stack, x, y, seed);
    }

    @Override
    public void renderFakeItem(@Nonnull ItemStack stack, int x, int y) {
        this.parent.renderFakeItem(stack, x, y);
    }

    @Override
    public void renderFakeItem(@Nonnull ItemStack stack, int x, int y, int seed) {
        this.parent.renderFakeItem(stack, x, y, seed);
    }

    @Override
    public void renderItem(@Nonnull LivingEntity entity, @Nonnull ItemStack stack, int x, int y, int seed) {
        this.parent.renderItem(entity, stack, x, y, seed);
    }

    @Override
    public void renderItemDecorations(@Nonnull Font font, @Nonnull ItemStack stack, int x, int y) {
        this.parent.renderItemDecorations(font, stack, x, y);
    }

    @Override
    public void renderItemDecorations(@Nonnull Font font, @Nonnull ItemStack stack, int x, int y, @Nullable String text) {
        this.parent.renderItemDecorations(font, stack, x, y, text);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Component text, int x, int y) {
        this.parent.setTooltipForNextFrame(text, x, y);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull List<FormattedCharSequence> lines, int x, int y) {
        this.parent.setTooltipForNextFrame(lines, x, y);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull ItemStack stack, int x, int y) {
        this.parent.setTooltipForNextFrame(font, stack, x, y);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull List<Component> textComponents, @Nonnull Optional<TooltipComponent> tooltipComponent, @Nonnull ItemStack stack, int mouseX, int mouseY) {
        this.parent.setTooltipForNextFrame(font, textComponents, tooltipComponent, stack, mouseX, mouseY);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull List<Component> textComponents, @Nonnull Optional<TooltipComponent> tooltipComponent, @Nonnull ItemStack stack, int mouseX, int mouseY, @Nullable Identifier backgroundTexture) {
        this.parent.setTooltipForNextFrame(font, textComponents, tooltipComponent, stack, mouseX, mouseY, backgroundTexture);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull List<Component> lines, @Nonnull Optional<TooltipComponent> tooltipImage, int x, int y) {
        this.parent.setTooltipForNextFrame(font, lines, tooltipImage, x, y);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull List<Component> lines, @Nonnull Optional<TooltipComponent> tooltipImage, int x, int y, @Nullable Identifier background) {
        this.parent.setTooltipForNextFrame(font, lines, tooltipImage, x, y, background);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull Component text, int x, int y) {
        this.parent.setTooltipForNextFrame(font, text, x, y);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull Component text, int x, int y, @Nullable Identifier background) {
        this.parent.setTooltipForNextFrame(font, text, x, y, background);
    }

    @Override
    public void setComponentTooltipForNextFrame(@Nonnull Font font, @Nonnull List<Component> lines, int x, int y) {
        this.parent.setComponentTooltipForNextFrame(font, lines, x, y);
    }

    @Override
    public void setComponentTooltipForNextFrame(@Nonnull Font font, @Nonnull List<Component> lines, int x, int y, @Nullable Identifier background) {
        this.parent.setComponentTooltipForNextFrame(font, lines, x, y, background);
    }

    @Override
    public void setComponentTooltipForNextFrame(@Nonnull Font p_font, @Nonnull List<? extends FormattedText> lines, int x, int y, @Nonnull ItemStack stack) {
        this.parent.setComponentTooltipForNextFrame(p_font, lines, x, y, stack);
    }

    @Override
    public void setComponentTooltipForNextFrame(@Nonnull Font p_font, @Nonnull List<? extends FormattedText> lines, int x, int y, @Nonnull ItemStack stack, @Nullable Identifier backgroundTexture) {
        this.parent.setComponentTooltipForNextFrame(p_font, lines, x, y, stack, backgroundTexture);
    }

    @Override
    public void setComponentTooltipFromElementsForNextFrame(@Nonnull Font font, @Nonnull List<Either<FormattedText, TooltipComponent>> elements, int mouseX, int mouseY, @Nonnull ItemStack stack) {
        this.parent.setComponentTooltipFromElementsForNextFrame(font, elements, mouseX, mouseY, stack);
    }

    @Override
    public void setComponentTooltipFromElementsForNextFrame(@Nonnull Font font, @Nonnull List<Either<FormattedText, TooltipComponent>> elements, int mouseX, int mouseY, @Nonnull ItemStack stack, @Nullable Identifier backgroundTexture) {
        this.parent.setComponentTooltipFromElementsForNextFrame(font, elements, mouseX, mouseY, stack, backgroundTexture);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull List<? extends FormattedCharSequence> lines, int x, int y) {
        this.parent.setTooltipForNextFrame(font, lines, x, y);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull List<? extends FormattedCharSequence> lines, int x, int y, @Nullable Identifier background) {
        this.parent.setTooltipForNextFrame(font, lines, x, y, background);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull List<FormattedCharSequence> lines, @Nonnull ClientTooltipPositioner positioner, int x, int y, boolean focused) {
        this.parent.setTooltipForNextFrame(font, lines, positioner, x, y, focused);
    }

    @Override
    public void renderTooltip(@Nonnull Font font, @Nonnull List<ClientTooltipComponent> components, int x, int y, @Nonnull ClientTooltipPositioner positioner, @Nullable Identifier background) {
        this.parent.renderTooltip(font, components, x, y, positioner, background);
    }

    @Override
    public void renderTooltip(@Nonnull Font font, @Nonnull List<ClientTooltipComponent> components, int x, int y, @Nonnull ClientTooltipPositioner positioner, @Nullable Identifier background, @Nonnull ItemStack tooltipStack) {
        this.parent.renderTooltip(font, components, x, y, positioner, background, tooltipStack);
    }

    @Override
    public void renderDeferredElements() {
        this.parent.renderDeferredElements();
    }

    @Override
    public void renderComponentHoverEffect(@Nonnull Font font, @Nullable Style style, int mouseX, int mouseY) {
        this.parent.renderComponentHoverEffect(font, style, mouseX, mouseY);
    }

    @Override
    public void submitMapRenderState(@Nonnull MapRenderState renderState) {
        this.parent.submitMapRenderState(renderState);
    }

    @Override
    public void submitEntityRenderState(@Nonnull EntityRenderState renderState, float scale, @Nonnull Vector3f translation, @Nonnull Quaternionf rotation, @Nullable Quaternionf overrideCameraAngle, int x0, int y0, int x1, int y1) {
        this.parent.submitEntityRenderState(renderState, scale, translation, rotation, overrideCameraAngle, x0, y0, x1, y1);
    }

    @Override
    public void submitSkinRenderState(@Nonnull PlayerModel playerModel, @Nonnull Identifier texture, float rotationX, float rotationY, float pivotY, float x0, int y0, int x1, int y1, int scale) {
        this.parent.submitSkinRenderState(playerModel, texture, rotationX, rotationY, pivotY, x0, y0, x1, y1, scale);
    }

    @Override
    public void submitBookModelRenderState(@Nonnull BookModel bookModel, @Nonnull Identifier texture, float open, float flip, float x0, int y0, int x1, int y1, int scale) {
        this.parent.submitBookModelRenderState(bookModel, texture, open, flip, x0, y0, x1, y1, scale);
    }

    @Override
    public void submitBannerPatternRenderState(@Nonnull BannerFlagModel flag, @Nonnull DyeColor baseColor, @Nonnull BannerPatternLayers resultBannerPatterns, int x0, int y0, int x1, int y1) {
        this.parent.submitBannerPatternRenderState(flag, baseColor, resultBannerPatterns, x0, y0, x1, y1);
    }

    @Override
    public void submitSignRenderState(@Nonnull Model.Simple signModel, float scale, @Nonnull WoodType woodType, int x0, int y0, int x1, int y1) {
        this.parent.submitSignRenderState(signModel, scale, woodType, x0, y0, x1, y1);
    }

    @Override
    public void submitProfilerChartRenderState(@Nonnull List<ResultField> chartData, int x0, int y0, int x1, int y1) {
        this.parent.submitProfilerChartRenderState(chartData, x0, y0, x1, y1);
    }

    @Override
    public void submitGuiElementRenderState(@Nonnull GuiElementRenderState renderState) {
        this.parent.submitGuiElementRenderState(renderState);
    }

    @Override
    public void submitPictureInPictureRenderState(@Nonnull PictureInPictureRenderState renderState) {
        this.parent.submitPictureInPictureRenderState(renderState);
    }

    @Override
    public @Nullable ScreenRectangle peekScissorStack() {
        return this.parent.peekScissorStack();
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getSprite(@Nonnull Material material) {
        return this.parent.getSprite(material);
    }

    @Nonnull
    @Override
    public ActiveTextCollector textRendererForWidget(@Nonnull AbstractWidget widget, @Nonnull HoveredTextEffects hoveredTextEffects) {
        return this.parent.textRendererForWidget(widget, hoveredTextEffects);
    }

    @Nonnull
    @Override
    public ActiveTextCollector textRenderer() {
        return this.parent.textRenderer();
    }

    @Nonnull
    @Override
    public ActiveTextCollector textRenderer(@Nonnull HoveredTextEffects hoveredTextEffects) {
        return this.parent.textRenderer(hoveredTextEffects);
    }

    @Nonnull
    @Override
    public ActiveTextCollector textRenderer(@Nonnull HoveredTextEffects hoveredTextEffects, @Nullable Consumer<Style> additionalConsumer) {
        return this.parent.textRenderer(hoveredTextEffects, additionalConsumer);
    }

    @Override
    public int getColorFromFormattingCharacter(char c, boolean isLighter) {
        return this.parent.getColorFromFormattingCharacter(c, isLighter);
    }

    @Override
    public void drawScrollingString(@Nonnull ActiveTextCollector textCollector, @Nonnull Font font, @Nonnull Component text, int minX, int maxX, int y) {
        this.parent.drawScrollingString(textCollector, font, text, minX, maxX, y);
    }

    @Override
    public void blitInscribed(@Nonnull Identifier texture, int x, int y, int boundsWidth, int boundsHeight, int rectWidth, int rectHeight) {
        this.parent.blitInscribed(texture, x, y, boundsWidth, boundsHeight, rectWidth, rectHeight);
    }

    @Override
    public void blitInscribed(@Nonnull Identifier texture, int x, int y, int boundsWidth, int boundsHeight, int rectWidth, int rectHeight, boolean centerX, boolean centerY) {
        this.parent.blitInscribed(texture, x, y, boundsWidth, boundsHeight, rectWidth, rectHeight, centerX, centerY);
    }
}

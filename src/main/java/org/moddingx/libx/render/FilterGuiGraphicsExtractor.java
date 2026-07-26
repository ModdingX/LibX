package org.moddingx.libx.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
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
 * A {@link GuiGraphicsExtractor} that overrides all methods and passes them through to the given parent. Useful if you
 * need to change the behavior of some methods in some specific part of code.
 */
public class FilterGuiGraphicsExtractor extends GuiGraphicsExtractor {

    protected final GuiGraphicsExtractor parent;

    public FilterGuiGraphicsExtractor(GuiGraphicsExtractor parent) {
        super(Minecraft.getInstance(), parent.guiRenderState, parent.mouseX, parent.mouseY);
        this.parent = parent;
    }

    @Override
    public void requestCursor(@Nonnull CursorType cursorType) {
        this.parent.requestCursor(cursorType);
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

    @Nonnull
    @Override
    public Matrix3x2fStack pose() {
        return this.parent.pose();
    }

    @Override
    public void nextStratum() {
        this.parent.nextStratum();
    }

    @Override
    public void blurBeforeThisStratum() {
        this.parent.blurBeforeThisStratum();
    }

    @Override
    public void enableScissor(int x0, int y0, int x1, int y1) {
        this.parent.enableScissor(x0, y0, x1, y1);
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
    public void horizontalLine(int x0, int x1, int y, int col) {
        this.parent.horizontalLine(x0, x1, y, col);
    }

    @Override
    public void verticalLine(int x, int y0, int y1, int col) {
        this.parent.verticalLine(x, y0, y1, col);
    }

    @Override
    public void fill(int x0, int y0, int x1, int y1, int col) {
        this.parent.fill(x0, y0, x1, y1, col);
    }

    @Override
    public void fill(@Nonnull RenderPipeline pipeline, int x0, int y0, int x1, int y1, int col) {
        this.parent.fill(pipeline, x0, y0, x1, y1, col);
    }

    @Override
    public void fillGradient(int x0, int y0, int x1, int y1, int col1, int col2) {
        this.parent.fillGradient(x0, y0, x1, y1, col1, col2);
    }

    @Override
    public void fill(@Nonnull RenderPipeline renderPipeline, @Nonnull TextureSetup textureSetup, int x0, int y0, int x1, int y1) {
        this.parent.fill(renderPipeline, textureSetup, x0, y0, x1, y1);
    }

    @Override
    public void outline(int x, int y, int width, int height, int color) {
        this.parent.outline(x, y, width, height, color);
    }

    @Override
    public void textHighlight(int x0, int y0, int x1, int y1, boolean invertText) {
        this.parent.textHighlight(x0, y0, x1, y1, invertText);
    }

    @Override
    public void text(@Nonnull Font font, @Nullable String str, int x, int y, int color) {
        this.parent.text(font, str, x, y, color);
    }

    @Override
    public void text(@Nonnull Font font, @Nullable String str, int x, int y, int color, boolean dropShadow) {
        this.parent.text(font, str, x, y, color, dropShadow);
    }

    @Override
    public void text(@Nonnull Font font, @Nonnull FormattedCharSequence str, int x, int y, int color) {
        this.parent.text(font, str, x, y, color);
    }

    @Override
    public void text(@Nonnull Font font, @Nonnull FormattedCharSequence str, int x, int y, int color, boolean dropShadow) {
        this.parent.text(font, str, x, y, color, dropShadow);
    }

    @Override
    public void text(@Nonnull Font font, @Nonnull Component str, int x, int y, int color) {
        this.parent.text(font, str, x, y, color);
    }

    @Override
    public void text(@Nonnull Font font, @Nonnull Component str, int x, int y, int color, boolean dropShadow) {
        this.parent.text(font, str, x, y, color, dropShadow);
    }

    @Override
    public void centeredText(@Nonnull Font font, @Nonnull String str, int x, int y, int color) {
        this.parent.centeredText(font, str, x, y, color);
    }

    @Override
    public void centeredText(@Nonnull Font font, @Nonnull Component text, int x, int y, int color) {
        this.parent.centeredText(font, text, x, y, color);
    }

    @Override
    public void centeredText(@Nonnull Font font, @Nonnull FormattedCharSequence text, int x, int y, int color) {
        this.parent.centeredText(font, text, x, y, color);
    }

    @Override
    public void textWithWordWrap(@Nonnull Font font, @Nonnull FormattedText string, int x, int y, int width, int col) {
        this.parent.textWithWordWrap(font, string, x, y, width, col);
    }

    @Override
    public void textWithWordWrap(@Nonnull Font font, @Nonnull FormattedText string, int x, int y, int width, int col, boolean dropShadow) {
        this.parent.textWithWordWrap(font, string, x, y, width, col, dropShadow);
    }

    @Override
    public void textWithBackdrop(@Nonnull Font font, @Nonnull Component str, int textX, int textY, int textWidth, int textColor) {
        this.parent.textWithBackdrop(font, str, textX, textY, textWidth, textColor);
    }

    @Override
    public void blit(@Nonnull RenderPipeline renderPipeline, @Nonnull Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color) {
        this.parent.blit(renderPipeline, texture, x, y, u, v, width, height, textureWidth, textureHeight, color);
    }

    @Override
    public void blit(@Nonnull RenderPipeline renderPipeline, @Nonnull Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        this.parent.blit(renderPipeline, texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    @Override
    public void blit(@Nonnull RenderPipeline renderPipeline, @Nonnull Identifier texture, int x, int y, float u, float v, int width, int height, int srcWidth, int srcHeight, int textureWidth, int textureHeight) {
        this.parent.blit(renderPipeline, texture, x, y, u, v, width, height, srcWidth, srcHeight, textureWidth, textureHeight);
    }

    @Override
    public void blit(@Nonnull RenderPipeline renderPipeline, @Nonnull Identifier texture, int x, int y, float u, float v, int width, int height, int srcWidth, int srcHeight, int textureWidth, int textureHeight, int color) {
        this.parent.blit(renderPipeline, texture, x, y, u, v, width, height, srcWidth, srcHeight, textureWidth, textureHeight, color);
    }

    @Override
    public void blit(@Nonnull Identifier location, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1) {
        this.parent.blit(location, x0, y0, x1, y1, u0, u1, v0, v1);
    }

    @Override
    public void blit(@Nonnull GpuTextureView textureView, @Nonnull GpuSampler sampler, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1) {
        this.parent.blit(textureView, sampler, x0, y0, x1, y1, u0, u1, v0, v1);
    }

    @Override
    public void blitSprite(@Nonnull RenderPipeline renderPipeline, @Nonnull Identifier location, int x, int y, int width, int height) {
        this.parent.blitSprite(renderPipeline, location, x, y, width, height);
    }

    @Override
    public void blitSprite(@Nonnull RenderPipeline renderPipeline, @Nonnull Identifier location, int x, int y, int width, int height, float alpha) {
        this.parent.blitSprite(renderPipeline, location, x, y, width, height, alpha);
    }

    @Override
    public void blitSprite(@Nonnull RenderPipeline renderPipeline, @Nonnull Identifier location, int x, int y, int width, int height, int color) {
        this.parent.blitSprite(renderPipeline, location, x, y, width, height, color);
    }

    @Override
    public void blitSprite(@Nonnull RenderPipeline renderPipeline, @Nonnull Identifier location, int spriteWidth, int spriteHeight, int textureX, int textureY, int x, int y, int width, int height) {
        this.parent.blitSprite(renderPipeline, location, spriteWidth, spriteHeight, textureX, textureY, x, y, width, height);
    }

    @Override
    public void blitSprite(@Nonnull RenderPipeline renderPipeline, @Nonnull Identifier location, int spriteWidth, int spriteHeight, int textureX, int textureY, int x, int y, int width, int height, int color) {
        this.parent.blitSprite(renderPipeline, location, spriteWidth, spriteHeight, textureX, textureY, x, y, width, height, color);
    }

    @Override
    public void blitSprite(@Nonnull RenderPipeline renderPipeline, @Nonnull TextureAtlasSprite sprite, int x, int y, int width, int height) {
        this.parent.blitSprite(renderPipeline, sprite, x, y, width, height);
    }

    @Override
    public void blitSprite(@Nonnull RenderPipeline renderPipeline, @Nonnull TextureAtlasSprite sprite, int x, int y, int width, int height, int color) {
        this.parent.blitSprite(renderPipeline, sprite, x, y, width, height, color);
    }

    @Override
    public void item(@Nonnull ItemStack itemStack, int x, int y) {
        this.parent.item(itemStack, x, y);
    }

    @Override
    public void item(@Nonnull ItemStack itemStack, int x, int y, int seed) {
        this.parent.item(itemStack, x, y, seed);
    }

    @Override
    public void item(@Nonnull LivingEntity owner, @Nonnull ItemStack itemStack, int x, int y, int seed) {
        this.parent.item(owner, itemStack, x, y, seed);
    }

    @Override
    public void fakeItem(@Nonnull ItemStack itemStack, int x, int y) {
        this.parent.fakeItem(itemStack, x, y);
    }

    @Override
    public void fakeItem(@Nonnull ItemStack itemStack, int x, int y, int seed) {
        this.parent.fakeItem(itemStack, x, y, seed);
    }

    @Override
    public void itemDecorations(@Nonnull Font font, @Nonnull ItemStack itemStack, int x, int y) {
        this.parent.itemDecorations(font, itemStack, x, y);
    }

    @Override
    public void itemDecorations(@Nonnull Font font, @Nonnull ItemStack itemStack, int x, int y, @Nullable String countText) {
        this.parent.itemDecorations(font, itemStack, x, y, countText);
    }

    @Override
    public void map(@Nonnull MapRenderState mapRenderState) {
        this.parent.map(mapRenderState);
    }

    @Override
    public void entity(@Nonnull EntityRenderState renderState, float scale, @Nonnull Vector3f translation, @Nonnull Quaternionf rotation, @Nullable Quaternionf overrideCameraAngle, int x0, int y0, int x1, int y1) {
        this.parent.entity(renderState, scale, translation, rotation, overrideCameraAngle, x0, y0, x1, y1);
    }

    @Override
    public void skin(@Nonnull PlayerModel playerModel, @Nonnull Identifier texture, float scale, float rotationX, float rotationY, float pivotY, int x0, int y0, int x1, int y1) {
        this.parent.skin(playerModel, texture, scale, rotationX, rotationY, pivotY, x0, y0, x1, y1);
    }

    @Override
    public void book(@Nonnull BookModel bookModel, @Nonnull Identifier texture, float scale, float open, float flip, int x0, int y0, int x1, int y1) {
        this.parent.book(bookModel, texture, scale, open, flip, x0, y0, x1, y1);
    }

    @Override
    public void bannerPattern(@Nonnull BannerFlagModel flag, @Nonnull DyeColor baseColor, @Nonnull BannerPatternLayers resultBannerPatterns, int x0, int y0, int x1, int y1) {
        this.parent.bannerPattern(flag, baseColor, resultBannerPatterns, x0, y0, x1, y1);
    }

    @Override
    public void sign(@Nonnull Model.Simple signModel, float scale, @Nonnull WoodType woodType, int x0, int y0, int x1, int y1) {
        this.parent.sign(signModel, scale, woodType, x0, y0, x1, y1);
    }

    @Override
    public void profilerChart(@Nonnull List<ResultField> chartData, int x0, int y0, int x1, int y1) {
        this.parent.profilerChart(chartData, x0, y0, x1, y1);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Component component, int x, int y) {
        this.parent.setTooltipForNextFrame(component, x, y);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull List<FormattedCharSequence> formattedCharSequences, int x, int y) {
        this.parent.setTooltipForNextFrame(formattedCharSequences, x, y);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull ItemStack itemStack, int xo, int yo) {
        this.parent.setTooltipForNextFrame(font, itemStack, xo, yo);
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
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull List<Component> texts, @Nonnull Optional<TooltipComponent> optionalImage, int xo, int yo) {
        this.parent.setTooltipForNextFrame(font, texts, optionalImage, xo, yo);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull List<Component> texts, @Nonnull Optional<TooltipComponent> optionalImage, int xo, int yo, @Nullable Identifier style) {
        this.parent.setTooltipForNextFrame(font, texts, optionalImage, xo, yo, style);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull List<FormattedCharSequence> tooltip, @Nonnull Optional<TooltipComponent> component, @Nonnull ClientTooltipPositioner positioner, int xo, int yo, boolean replaceExisting, @Nullable Identifier style) {
        this.parent.setTooltipForNextFrame(font, tooltip, component, positioner, xo, yo, replaceExisting, style);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull Component text, int xo, int yo) {
        this.parent.setTooltipForNextFrame(font, text, xo, yo);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull Component text, int xo, int yo, @Nullable Identifier style) {
        this.parent.setTooltipForNextFrame(font, text, xo, yo, style);
    }

    @Override
    public void setComponentTooltipForNextFrame(@Nonnull Font font, @Nonnull List<Component> lines, int xo, int yo) {
        this.parent.setComponentTooltipForNextFrame(font, lines, xo, yo);
    }

    @Override
    public void setComponentTooltipForNextFrame(@Nonnull Font font, @Nonnull List<Component> lines, int xo, int yo, @Nullable Identifier style) {
        this.parent.setComponentTooltipForNextFrame(font, lines, xo, yo, style);
    }

    @Override
    public void setComponentTooltipForNextFrame(@Nonnull Font font, @Nonnull List<? extends FormattedText> tooltips, int mouseX, int mouseY, @Nonnull ItemStack stack) {
        this.parent.setComponentTooltipForNextFrame(font, tooltips, mouseX, mouseY, stack);
    }

    @Override
    public void setComponentTooltipForNextFrame(@Nonnull Font font, @Nonnull List<? extends FormattedText> tooltips, int mouseX, int mouseY, @Nonnull ItemStack stack, @Nullable Identifier backgroundTexture) {
        this.parent.setComponentTooltipForNextFrame(font, tooltips, mouseX, mouseY, stack, backgroundTexture);
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
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull List<? extends FormattedCharSequence> lines, int xo, int yo) {
        this.parent.setTooltipForNextFrame(font, lines, xo, yo);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull List<? extends FormattedCharSequence> lines, int xo, int yo, @Nullable Identifier style) {
        this.parent.setTooltipForNextFrame(font, lines, xo, yo, style);
    }

    @Override
    public void setTooltipForNextFrame(@Nonnull Font font, @Nonnull List<FormattedCharSequence> tooltip, @Nonnull ClientTooltipPositioner positioner, int xo, int yo, boolean replaceExisting) {
        this.parent.setTooltipForNextFrame(font, tooltip, positioner, xo, yo, replaceExisting);
    }

    @Override
    public void tooltip(@Nonnull Font font, @Nonnull List<ClientTooltipComponent> lines, int xo, int yo, @Nonnull ClientTooltipPositioner positioner, @Nullable Identifier style) {
        this.parent.tooltip(font, lines, xo, yo, positioner, style);
    }

    @Override
    public void tooltip(@Nonnull Font font, @Nonnull List<ClientTooltipComponent> lines, int xo, int yo, @Nonnull ClientTooltipPositioner positioner, @Nullable Identifier style, @Nonnull ItemStack tooltipStack) {
        this.parent.tooltip(font, lines, xo, yo, positioner, style, tooltipStack);
    }

    @Override
    public void setPreeditOverlay(@Nonnull Renderable preeditOverlay) {
        this.parent.setPreeditOverlay(preeditOverlay);
    }

    @Override
    public void extractDeferredElements(int mouseX, int mouseY, float a) {
        this.parent.extractDeferredElements(mouseX, mouseY, a);
    }

    @Override
    public void componentHoverEffect(@Nonnull Font font, @Nonnull Style hoveredStyle, int xMouse, int yMouse) {
        this.parent.componentHoverEffect(font, hoveredStyle, xMouse, yMouse);
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
    public TextureAtlasSprite getSprite(@Nonnull SpriteId sprite) {
        return this.parent.getSprite(sprite);
    }

    @Nonnull
    @Override
    public ActiveTextCollector textRendererForWidget(@Nonnull AbstractWidget owner, @Nonnull HoveredTextEffects hoveredTextEffects) {
        return this.parent.textRendererForWidget(owner, hoveredTextEffects);
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
    public ActiveTextCollector textRenderer(@Nonnull HoveredTextEffects hoveredTextEffects, @Nullable Consumer<Style> additionalHoverStyleConsumer) {
        return this.parent.textRenderer(hoveredTextEffects, additionalHoverStyleConsumer);
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

package org.moddingx.libx.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
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
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A {@link GuiGraphics} that overrides all methods and passes them through to the given parent. Useful if you
 * need to change the behaviour of some methods in some specific part of code.
 */
public class FilterGuiGraphics extends GuiGraphics {
    
    protected final GuiGraphics parent;
    
    public FilterGuiGraphics(GuiGraphics parent) {
        super(Minecraft.getInstance(), parent.bufferSource);
        this.parent = parent;
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
    public PoseStack pose() {
        return this.parent.pose();
    }

    @Override
    public void flush() {
        this.parent.flush();
    }

    @Override
    public void hLine(int p_283318_, int p_281662_, int p_281346_, int p_281672_) {
        this.parent.hLine(p_283318_, p_281662_, p_281346_, p_281672_);
    }

    @Override
    public void hLine(@Nonnull RenderType p_286630_, int p_286453_, int p_286247_, int p_286814_, int p_286623_) {
        this.parent.hLine(p_286630_, p_286453_, p_286247_, p_286814_, p_286623_);
    }

    @Override
    public void vLine(int p_282951_, int p_281591_, int p_281568_, int p_282718_) {
        this.parent.vLine(p_282951_, p_281591_, p_281568_, p_282718_);
    }

    @Override
    public void vLine(@Nonnull RenderType p_286607_, int p_286309_, int p_286480_, int p_286707_, int p_286855_) {
        this.parent.vLine(p_286607_, p_286309_, p_286480_, p_286707_, p_286855_);
    }

    @Override
    public void enableScissor(int p_281479_, int p_282788_, int p_282924_, int p_282826_) {
        this.parent.enableScissor(p_281479_, p_282788_, p_282924_, p_282826_);
    }

    @Override
    public void disableScissor() {
        this.parent.disableScissor();
    }

    @Override
    public boolean containsPointInScissor(int p_332689_, int p_332771_) {
        return this.parent.containsPointInScissor(p_332689_, p_332771_);
    }

    @Override
    public void fill(int p_282988_, int p_282861_, int p_281278_, int p_281710_, int p_281470_) {
        this.parent.fill(p_282988_, p_282861_, p_281278_, p_281710_, p_281470_);
    }

    @Override
    public void fill(int p_281437_, int p_283660_, int p_282606_, int p_283413_, int p_283428_, int p_283253_) {
        this.parent.fill(p_281437_, p_283660_, p_282606_, p_283413_, p_283428_, p_283253_);
    }

    @Override
    public void fill(@Nonnull RenderType p_286602_, int p_286738_, int p_286614_, int p_286741_, int p_286610_, int p_286560_) {
        this.parent.fill(p_286602_, p_286738_, p_286614_, p_286741_, p_286610_, p_286560_);
    }

    @Override
    public void fill(@Nonnull RenderType p_286711_, int p_286234_, int p_286444_, int p_286244_, int p_286411_, int p_286671_, int p_286599_) {
        this.parent.fill(p_286711_, p_286234_, p_286444_, p_286244_, p_286411_, p_286671_, p_286599_);
    }

    @Override
    public void fillGradient(int p_283290_, int p_283278_, int p_282670_, int p_281698_, int p_283374_, int p_283076_) {
        this.parent.fillGradient(p_283290_, p_283278_, p_282670_, p_281698_, p_283374_, p_283076_);
    }

    @Override
    public void fillGradient(int p_282702_, int p_282331_, int p_281415_, int p_283118_, int p_282419_, int p_281954_, int p_282607_) {
        this.parent.fillGradient(p_282702_, p_282331_, p_281415_, p_283118_, p_282419_, p_281954_, p_282607_);
    }

    @Override
    public void fillGradient(@Nonnull RenderType p_286522_, int p_286535_, int p_286839_, int p_286242_, int p_286856_, int p_286809_, int p_286833_, int p_286706_) {
        this.parent.fillGradient(p_286522_, p_286535_, p_286839_, p_286242_, p_286856_, p_286809_, p_286833_, p_286706_);
    }

    @Override
    public void fillRenderType(@Nonnull RenderType p_331805_, int p_330261_, int p_330693_, int p_331143_, int p_331708_, int p_330497_) {
        this.parent.fillRenderType(p_331805_, p_330261_, p_330693_, p_331143_, p_331708_, p_330497_);
    }

    @Override
    public void drawCenteredString(@Nonnull Font p_282122_, @Nonnull String p_282898_, int p_281490_, int p_282853_, int p_281258_) {
        this.parent.drawCenteredString(p_282122_, p_282898_, p_281490_, p_282853_, p_281258_);
    }

    @Override
    public void drawCenteredString(@Nonnull Font p_282901_, @Nonnull Component p_282456_, int p_283083_, int p_282276_, int p_281457_) {
        this.parent.drawCenteredString(p_282901_, p_282456_, p_283083_, p_282276_, p_281457_);
    }

    @Override
    public void drawCenteredString(@Nonnull Font p_282592_, @Nonnull FormattedCharSequence p_281854_, int p_281573_, int p_283511_, int p_282577_) {
        this.parent.drawCenteredString(p_282592_, p_281854_, p_281573_, p_283511_, p_282577_);
    }

    @Override
    public int drawString(@Nonnull Font p_282003_, @Nullable String p_281403_, int p_282714_, int p_282041_, int p_281908_) {
        return this.parent.drawString(p_282003_, p_281403_, p_282714_, p_282041_, p_281908_);
    }

    @Override
    public int drawString(@Nonnull Font p_283343_, @Nullable String p_281896_, int p_283569_, int p_283418_, int p_281560_, boolean p_282130_) {
        return this.parent.drawString(p_283343_, p_281896_, p_283569_, p_283418_, p_281560_, p_282130_);
    }

    @Override
    public int drawString(@Nonnull Font p_283343_, @Nullable String p_281896_, float p_283569_, float p_283418_, int p_281560_, boolean p_282130_) {
        return this.parent.drawString(p_283343_, p_281896_, p_283569_, p_283418_, p_281560_, p_282130_);
    }

    @Override
    public int drawString(@Nonnull Font p_283019_, @Nonnull FormattedCharSequence p_283376_, int p_283379_, int p_283346_, int p_282119_) {
        return this.parent.drawString(p_283019_, p_283376_, p_283379_, p_283346_, p_282119_);
    }

    @Override
    public int drawString(@Nonnull Font p_282636_, @Nonnull FormattedCharSequence p_281596_, int p_281586_, int p_282816_, int p_281743_, boolean p_282394_) {
        return this.parent.drawString(p_282636_, p_281596_, p_281586_, p_282816_, p_281743_, p_282394_);
    }

    @Override
    public int drawString(@Nonnull Font p_282636_, @Nonnull FormattedCharSequence p_281596_, float p_281586_, float p_282816_, int p_281743_, boolean p_282394_) {
        return this.parent.drawString(p_282636_, p_281596_, p_281586_, p_282816_, p_281743_, p_282394_);
    }

    @Override
    public int drawString(@Nonnull Font p_281653_, @Nonnull Component p_283140_, int p_283102_, int p_282347_, int p_281429_) {
        return this.parent.drawString(p_281653_, p_283140_, p_283102_, p_282347_, p_281429_);
    }

    @Override
    public int drawString(@Nonnull Font p_281547_, @Nonnull Component p_282131_, int p_282857_, int p_281250_, int p_282195_, boolean p_282791_) {
        return this.parent.drawString(p_281547_, p_282131_, p_282857_, p_281250_, p_282195_, p_282791_);
    }

    @Override
    public void drawWordWrap(@Nonnull Font p_281494_, @Nonnull FormattedText p_283463_, int p_282183_, int p_283250_, int p_282564_, int p_282629_) {
        this.parent.drawWordWrap(p_281494_, p_283463_, p_282183_, p_283250_, p_282564_, p_282629_);
    }

    @Override
    public int drawStringWithBackdrop(@Nonnull Font p_348650_, @Nonnull Component p_348614_, int p_348465_, int p_348495_, int p_348581_, int p_348666_) {
        return this.parent.drawStringWithBackdrop(p_348650_, p_348614_, p_348465_, p_348495_, p_348581_, p_348666_);
    }

    @Override
    public void renderOutline(int p_281496_, int p_282076_, int p_281334_, int p_283576_, int p_283618_) {
        this.parent.renderOutline(p_281496_, p_282076_, p_281334_, p_283576_, p_283618_);
    }

    @Override
    public void blitSprite(@Nonnull Function<ResourceLocation, RenderType> p_363890_, @Nonnull ResourceLocation p_294915_, int p_295058_, int p_294415_, int p_294535_, int p_295510_) {
        this.parent.blitSprite(p_363890_, p_294915_, p_295058_, p_294415_, p_294535_, p_295510_);
    }

    @Override
    public void blitSprite(@Nonnull Function<ResourceLocation, RenderType> p_365436_, @Nonnull ResourceLocation p_365379_, int p_294695_, int p_296458_, int p_294279_, int p_295235_, int p_295034_) {
        this.parent.blitSprite(p_365436_, p_365379_, p_294695_, p_296458_, p_294279_, p_295235_, p_295034_);
    }

    @Override
    public void blitSprite(@Nonnull Function<ResourceLocation, RenderType> p_364966_, @Nonnull ResourceLocation p_294549_, int p_294560_, int p_295075_, int p_294098_, int p_295872_, int p_294414_, int p_362199_, int p_363608_, int p_365523_) {
        this.parent.blitSprite(p_364966_, p_294549_, p_294560_, p_295075_, p_294098_, p_295872_, p_294414_, p_362199_, p_363608_, p_365523_);
    }

    @Override
    public void blitSprite(@Nonnull Function<ResourceLocation, RenderType> p_364096_, @Nonnull TextureAtlasSprite p_361089_, int p_294223_, int p_296245_, int p_296255_, int p_295669_) {
        this.parent.blitSprite(p_364096_, p_361089_, p_294223_, p_296245_, p_296255_, p_295669_);
    }

    @Override
    public void blitSprite(@Nonnull Function<ResourceLocation, RenderType> p_363285_, @Nonnull TextureAtlasSprite p_364680_, int p_295194_, int p_295164_, int p_294823_, int p_295650_, int p_295401_) {
        this.parent.blitSprite(p_363285_, p_364680_, p_295194_, p_295164_, p_294823_, p_295650_, p_295401_);
    }

    @Override
    public void blit(@Nonnull Function<ResourceLocation, RenderType> p_363559_, @Nonnull ResourceLocation p_282034_, int p_283671_, int p_282377_, float p_282285_, float p_283199_, int p_282058_, int p_281939_, int p_282186_, int p_282322_, int p_282481_) {
        this.parent.blit(p_363559_, p_282034_, p_283671_, p_282377_, p_282285_, p_283199_, p_282058_, p_281939_, p_282186_, p_282322_, p_282481_);
    }

    @Override
    public void blit(@Nonnull Function<ResourceLocation, RenderType> p_361481_, @Nonnull ResourceLocation p_283573_, int p_283574_, int p_283670_, float p_283029_, float p_283061_, int p_283545_, int p_282845_, int p_282558_, int p_282832_) {
        this.parent.blit(p_361481_, p_283573_, p_283574_, p_283670_, p_283029_, p_283061_, p_283545_, p_282845_, p_282558_, p_282832_);
    }

    @Override
    public void blit(@Nonnull Function<ResourceLocation, RenderType> p_361404_, @Nonnull ResourceLocation p_282639_, int p_282732_, int p_283541_, float p_282660_, float p_281522_, int p_281760_, int p_283298_, int p_283429_, int p_282193_, int p_281980_, int p_282315_) {
        this.parent.blit(p_361404_, p_282639_, p_282732_, p_283541_, p_282660_, p_281522_, p_281760_, p_283298_, p_283429_, p_282193_, p_281980_, p_282315_);
    }

    @Override
    public void blit(@Nonnull Function<ResourceLocation, RenderType> p_363000_, @Nonnull ResourceLocation p_363701_, int p_282225_, int p_281487_, float p_363958_, float p_363869_, int p_281985_, int p_281329_, int p_283035_, int p_363829_, int p_365041_, int p_361356_, int p_363808_) {
        this.parent.blit(p_363000_, p_363701_, p_282225_, p_281487_, p_363958_, p_363869_, p_281985_, p_281329_, p_283035_, p_363829_, p_365041_, p_361356_, p_363808_);
    }

    @Override
    public void innerBlit(@Nonnull Function<ResourceLocation, RenderType> p_362282_, @Nonnull ResourceLocation p_283254_, int p_283092_, int p_281930_, int p_282113_, int p_281388_, float p_281327_, float p_281676_, float p_283166_, float p_282630_, int p_283583_) {
        this.parent.innerBlit(p_362282_, p_283254_, p_283092_, p_281930_, p_282113_, p_281388_, p_281327_, p_281676_, p_283166_, p_282630_, p_283583_);
    }

    @Override
    public void renderItem(@Nonnull ItemStack p_281978_, int p_282647_, int p_281944_) {
        this.parent.renderItem(p_281978_, p_282647_, p_281944_);
    }

    @Override
    public void renderItem(@Nonnull ItemStack p_282262_, int p_283221_, int p_283496_, int p_283435_) {
        this.parent.renderItem(p_282262_, p_283221_, p_283496_, p_283435_);
    }

    @Override
    public void renderItem(@Nonnull ItemStack p_282786_, int p_282502_, int p_282976_, int p_281592_, int p_282314_) {
        this.parent.renderItem(p_282786_, p_282502_, p_282976_, p_281592_, p_282314_);
    }

    @Override
    public void renderFakeItem(@Nonnull ItemStack p_281946_, int p_283299_, int p_283674_) {
        this.parent.renderFakeItem(p_281946_, p_283299_, p_283674_);
    }

    @Override
    public void renderFakeItem(@Nonnull ItemStack p_312904_, int p_312257_, int p_312674_, int p_312138_) {
        this.parent.renderFakeItem(p_312904_, p_312257_, p_312674_, p_312138_);
    }

    @Override
    public void renderItem(@Nonnull LivingEntity p_282154_, @Nonnull ItemStack p_282777_, int p_282110_, int p_281371_, int p_283572_) {
        this.parent.renderItem(p_282154_, p_282777_, p_282110_, p_281371_, p_283572_);
    }

    @Override
    public void renderItemDecorations(@Nonnull Font p_281721_, @Nonnull ItemStack p_281514_, int p_282056_, int p_282683_) {
        this.parent.renderItemDecorations(p_281721_, p_281514_, p_282056_, p_282683_);
    }

    @Override
    public void renderItemDecorations(@Nonnull Font p_282005_, @Nonnull ItemStack p_283349_, int p_282641_, int p_282146_, @Nullable String p_282803_) {
        this.parent.renderItemDecorations(p_282005_, p_283349_, p_282641_, p_282146_, p_282803_);
    }

    @Override
    public void renderTooltip(@Nonnull Font p_282308_, @Nonnull ItemStack p_282781_, int p_282687_, int p_282292_) {
        this.parent.renderTooltip(p_282308_, p_282781_, p_282687_, p_282292_);
    }

    @Override
    public void renderTooltip(@Nonnull Font font, @Nonnull List<Component> textComponents, @Nonnull Optional<TooltipComponent> tooltipComponent, @Nonnull ItemStack stack, int mouseX, int mouseY) {
        this.parent.renderTooltip(font, textComponents, tooltipComponent, stack, mouseX, mouseY);
    }

    @Override
    public void renderTooltip(@Nonnull Font font, @Nonnull List<Component> textComponents, @Nonnull Optional<TooltipComponent> tooltipComponent, @Nonnull ItemStack stack, int mouseX, int mouseY, @Nullable ResourceLocation backgroundTexture) {
        this.parent.renderTooltip(font, textComponents, tooltipComponent, stack, mouseX, mouseY, backgroundTexture);
    }

    @Override
    public void renderTooltip(@Nonnull Font p_283128_, @Nonnull List<Component> p_282716_, @Nonnull Optional<TooltipComponent> p_281682_, int p_283678_, int p_281696_) {
        this.parent.renderTooltip(p_283128_, p_282716_, p_281682_, p_283678_, p_281696_);
    }

    @Override
    public void renderTooltip(@Nonnull Font p_371715_, @Nonnull List<Component> p_371741_, @Nonnull Optional<TooltipComponent> p_371604_, int p_371500_, int p_371755_, @Nullable ResourceLocation p_371766_) {
        this.parent.renderTooltip(p_371715_, p_371741_, p_371604_, p_371500_, p_371755_, p_371766_);
    }

    @Override
    public void renderTooltip(@Nonnull Font p_282269_, @Nonnull Component p_282572_, int p_282044_, int p_282545_) {
        this.parent.renderTooltip(p_282269_, p_282572_, p_282044_, p_282545_);
    }

    @Override
    public void renderTooltip(@Nonnull Font p_373080_, @Nonnull Component p_372937_, int p_372898_, int p_372815_, @Nullable ResourceLocation p_373023_) {
        this.parent.renderTooltip(p_373080_, p_372937_, p_372898_, p_372815_, p_373023_);
    }

    @Override
    public void renderComponentTooltip(@Nonnull Font p_282739_, @Nonnull List<Component> p_281832_, int p_282191_, int p_282446_) {
        this.parent.renderComponentTooltip(p_282739_, p_281832_, p_282191_, p_282446_);
    }

    @Override
    public void renderComponentTooltip(@Nonnull Font p_371677_, @Nonnull List<Component> p_371519_, int p_371314_, int p_371389_, @Nullable ResourceLocation p_371458_) {
        this.parent.renderComponentTooltip(p_371677_, p_371519_, p_371314_, p_371389_, p_371458_);
    }

    @Override
    public void renderComponentTooltip(@Nonnull Font font, @Nonnull List<? extends FormattedText> tooltips, int mouseX, int mouseY, @Nonnull ItemStack stack) {
        this.parent.renderComponentTooltip(font, tooltips, mouseX, mouseY, stack);
    }

    @Override
    public void renderComponentTooltip(@Nonnull Font font, @Nonnull List<? extends FormattedText> tooltips, int mouseX, int mouseY, @Nonnull ItemStack stack, @Nullable ResourceLocation backgroundTexture) {
        this.parent.renderComponentTooltip(font, tooltips, mouseX, mouseY, stack, backgroundTexture);
    }

    @Override
    public void renderComponentTooltipFromElements(@Nonnull Font font, @Nonnull List<Either<FormattedText, TooltipComponent>> elements, int mouseX, int mouseY, @Nonnull ItemStack stack) {
        this.parent.renderComponentTooltipFromElements(font, elements, mouseX, mouseY, stack);
    }

    @Override
    public void renderComponentTooltipFromElements(@Nonnull Font font, @Nonnull List<Either<FormattedText, TooltipComponent>> elements, int mouseX, int mouseY, @Nonnull ItemStack stack, @Nullable ResourceLocation backgroundTexture) {
        this.parent.renderComponentTooltipFromElements(font, elements, mouseX, mouseY, stack, backgroundTexture);
    }

    @Override
    public void renderTooltip(@Nonnull Font p_282192_, @Nonnull List<? extends FormattedCharSequence> p_282297_, int p_281680_, int p_283325_) {
        this.parent.renderTooltip(p_282192_, p_282297_, p_281680_, p_283325_);
    }

    @Override
    public void renderTooltip(@Nonnull Font p_373106_, @Nonnull List<? extends FormattedCharSequence> p_373020_, int p_372927_, int p_372819_, @Nullable ResourceLocation p_372954_) {
        this.parent.renderTooltip(p_373106_, p_373020_, p_372927_, p_372819_, p_372954_);
    }

    @Override
    public void renderTooltip(@Nonnull Font p_281627_, @Nonnull List<FormattedCharSequence> p_283313_, @Nonnull ClientTooltipPositioner p_283571_, int p_282367_, int p_282806_) {
        this.parent.renderTooltip(p_281627_, p_283313_, p_283571_, p_282367_, p_282806_);
    }

    @Override
    public void renderComponentHoverEffect(@Nonnull Font p_282584_, @Nullable Style p_282156_, int p_283623_, int p_282114_) {
        this.parent.renderComponentHoverEffect(p_282584_, p_282156_, p_283623_, p_282114_);
    }

    @Override
    public void drawSpecial(@Nonnull Consumer<MultiBufferSource> p_371453_) {
        this.parent.drawSpecial(p_371453_);
    }

    @Override
    public int getColorFromFormattingCharacter(char c, boolean isLighter) {
        return this.parent.getColorFromFormattingCharacter(c, isLighter);
    }

    @Override
    public int drawScrollingString(@Nonnull Font font, @Nonnull Component text, int minX, int maxX, int y, int color) {
        return this.parent.drawScrollingString(font, text, minX, maxX, y, color);
    }

    @Override
    public void blitInscribed(@Nonnull ResourceLocation texture, int x, int y, int boundsWidth, int boundsHeight, int rectWidth, int rectHeight) {
        this.parent.blitInscribed(texture, x, y, boundsWidth, boundsHeight, rectWidth, rectHeight);
    }

    @Override
    public void blitInscribed(@Nonnull ResourceLocation texture, int x, int y, int boundsWidth, int boundsHeight, int rectWidth, int rectHeight, boolean centerX, boolean centerY) {
        this.parent.blitInscribed(texture, x, y, boundsWidth, boundsHeight, rectWidth, rectHeight, centerX, centerY);
    }
}

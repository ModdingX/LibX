package org.moddingx.libx.screen;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2f;
import org.moddingx.libx.render.RenderHelper;
import org.moddingx.libx.util.lazy.CachedValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A widget that lets a user select a {@link TextColor colour}.
 */
public class ColorPicker extends Panel {

    /**
     * The width of a color picker widget
     */
    public static final int WIDTH = 200;

    /**
     * The height of a color picker widget
     */
    public static final int HEIGHT = 100;

    // Amount of vertices (vertically) for the hue bar
    private static final int HUE_VERTICES = 40;

    // Amount of vertices (horizontally & vertically) for the saturation & brightness square
    private static final int HSB_VERTICES = 20;

    private int red;
    private int green;
    private int blue;

    private float hue;
    private float saturation;
    private float brightness;

    private final CachedValue<TextColor> colorValue;

    private final List<VertexInfo> huePanel;
    private final CachedValue<List<VertexInfo>> hsbMatrix;

    private final ValueSlider redSlider;
    private final ValueSlider greenSlider;
    private final ValueSlider blueSlider;

    @Nullable
    private Consumer<TextColor> responder;
    private TextColor lastDelivered = null;

    private boolean enabled;

    public ColorPicker(int x, int y) {
        this(x, y, null);
    }

    public ColorPicker(int x, int y, @Nullable ColorPicker old) {
        super(x, y, WIDTH, HEIGHT);

        this.colorValue = new CachedValue<>(() -> TextColor.fromRgb(((this.red & 0xFF) << 16) | ((this.green & 0xFF) << 8) | (this.blue & 0xFF)));

        this.huePanel = createHuePanel(105, 0, 110, 100);
        this.hsbMatrix = new CachedValue<>(() -> createHsbMatrix(this.hue, 0, 0, 100, 100));

        this.redSlider = this.addRenderableWidget(new ValueSlider(115, 0, 85, 20, "libx.gui.color_picker.red", () -> this.red, red -> {
            this.red = red;
            this.updateHSB();
        }));
        this.greenSlider = this.addRenderableWidget(new ValueSlider(115, 23, 85, 20, "libx.gui.color_picker.green", () -> this.green, green -> {
            this.green = green;
            this.updateHSB();
        }));
        this.blueSlider = this.addRenderableWidget(new ValueSlider(115, 46, 85, 20, "libx.gui.color_picker.blue", () -> this.blue, blue -> {
            this.blue = blue;
            this.updateHSB();
        }));
        if (old != null) {
            this.setColor(old.getColor());
        } else {
            this.update();
        }
        this.enabled = true;
    }

    /**
     * Gets the current value of the color picker.
     */
    public TextColor getColor() {
        return this.colorValue.get();
    }

    /**
     * Sets the current value of the color picker.
     */
    public void setColor(TextColor color) {
        int rgb = 0xFFFFFF & color.getValue();
        this.red = (rgb >> 16) & 0xFF;
        this.green = (rgb >> 8) & 0xFF;
        this.blue = rgb & 0xFF;
        this.updateHSB();
    }

    /**
     * Sets a responder that is notified whenever the color changes.
     */
    public void setResponder(@Nullable Consumer<TextColor> responder) {
        this.responder = responder;
        this.lastDelivered = null;
    }

    private void updateRGB() {
        int rgb = 0xFFFFFF & Color.HSBtoRGB(this.hue, this.saturation, this.brightness);
        this.red = (rgb >> 16) & 0xFF;
        this.green = (rgb >> 8) & 0xFF;
        this.blue = rgb & 0xFF;
        this.update();
    }

    private void updateHSB() {
        float[] values = Color.RGBtoHSB(this.red, this.green, this.blue, null);
        this.hue = values[0];
        this.saturation = values[1];
        this.brightness = values[2];
        this.update();
    }

    private void update() {
        this.colorValue.invalidate();
        this.hsbMatrix.invalidate();

        this.redSlider.update();
        this.greenSlider.update();
        this.blueSlider.update();

        if (this.responder != null) {
            if (this.lastDelivered == null || this.lastDelivered.getValue() != this.colorValue.get().getValue()) {
                this.responder.accept(this.colorValue.get());
                this.lastDelivered = this.colorValue.get();
            }
        }
    }

    @Override
    public void extractWidgetContent(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(this.getX(), this.getY());

        // Capture the current transform matrix as a snapshot for deferred rendering
        Matrix3x2f matrix = new Matrix3x2f(graphics.pose());
        List<VertexInfo> hsbList = this.hsbMatrix.get();
        List<VertexInfo> huePanelList = this.huePanel;
        boolean isEnabled = this.enabled;

        graphics.submitGuiElementRenderState(new GuiElementRenderState() {
            @Override
            public void buildVertices(@Nonnull VertexConsumer consumer) {
                if (isEnabled) {
                    hsbList.forEach(v -> v.add(consumer, matrix));
                    huePanelList.forEach(v -> v.add(consumer, matrix));
                } else {
                    hsbList.forEach(v -> v.addGrayscale(consumer, matrix));
                    huePanelList.forEach(v -> v.addGrayscale(consumer, matrix));
                }
            }

            @Nonnull
            @Override
            public RenderPipeline pipeline() {
                return RenderPipelines.GUI;
            }

            @Nonnull
            @Override
            public TextureSetup textureSetup() {
                return TextureSetup.noTexture();
            }

            @Nullable
            @Override
            public ScreenRectangle scissorArea() {
                return null;
            }

            @Nonnull
            @Override
            public ScreenRectangle bounds() {
                return new ScreenRectangle((int) matrix.m20(), (int) matrix.m21(), 110, 100);
            }
        });

        int colorValue = ((this.red & 0xFF) << 16) | ((this.green & 0xFF) << 8) | (this.blue & 0xFF);
        int displayColor = colorValue;
        if (!this.enabled) {
            int value = Math.round((this.red + this.green + this.blue) / 3f) & 0xFF;
            displayColor = (value << 16) | (value << 8) | value;
        }

        int highlightColor = this.brightness > 0.5 ? 0xFF000000 : 0xFFFFFFFF;

        graphics.blit(RenderPipelines.GUI_TEXTURED, RenderHelper.TEXTURE_WHITE, 115, 69, 0, 0, 85, 31, 256, 256, highlightColor);
        graphics.blit(RenderPipelines.GUI_TEXTURED, RenderHelper.TEXTURE_WHITE, 116, 70, 0, 0, 83, 29, 256, 256, 0xFF000000 | displayColor);

        String colorText = String.format("#%06X", colorValue);
        graphics.nextStratum();
        graphics.text(Minecraft.getInstance().font, colorText, 157 - (Minecraft.getInstance().font.width(colorText) / 2), 80, highlightColor, false);

        graphics.pose().popMatrix();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (super.mouseClicked(event, isDoubleClick)) {
            return true;
        } else {
            return this.updateColorValue(event.x(), event.y(), event.x(), event.y());
        }
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (super.mouseDragged(event, dragX, dragY)) {
            return true;
        } else {
            return this.updateColorValue(event.x(), event.y(), event.x() - dragX, event.y() - dragY);
        }
    }

    private boolean updateColorValue(double mouseX, double mouseY, double boundsX, double boundsY) {
        if (!this.enabled) return false;
        // boundsX and boundsY contains the last mouse position when dragging
        // required to make it possible to get values from the border of the color grid.
        mouseX -= this.getX();
        mouseY -= this.getY();
        boundsX -= this.getX();
        boundsY -= this.getY();
        if (boundsX >= 0 && boundsX <= 100 && boundsY >= 0 && boundsY <= 100) {
            this.saturation = (float) (Mth.clamp(mouseX, 0, 100) / (float) 100);
            this.brightness = (float) (1 - (Mth.clamp(mouseY, 0, 100) / (float) 100));
            this.updateRGB();
            this.setFocused(null);
            return true;
        } else if (boundsX >= 105 && boundsX <= 110 && boundsY >= 0 && boundsY <= 100) {
            this.hue = (float) (Mth.clamp(mouseY, 0, 100) / (float) 100);
            this.updateRGB();
            this.setFocused(null);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void enabled(boolean enabled) {
        super.enabled(enabled);
        this.enabled = enabled;
    }

    private record ColorValue(int red, int green, int blue) {

        public static ColorValue create(int value) {
            int rgb = 0xFFFFFF & value;
            return new ColorValue((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
        }
    }

    private record VertexInfo(float x, float y, ColorValue color) {

        public void add(VertexConsumer vertex, Matrix3x2f matrix) {
            vertex.addVertexWith2DPose(matrix, this.x, this.y).setColor(this.color.red, this.color.green, this.color.blue, 255);
        }

        public void addGrayscale(VertexConsumer vertex, Matrix3x2f matrix) {
            int value = Math.round((this.color.red + this.color.green + this.color.blue) / 3f);
            vertex.addVertexWith2DPose(matrix, this.x, this.y).setColor(value, value, value, 255);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static List<VertexInfo> createHsbMatrix(float hue, int x1, int y1, int x2, int y2) {
        LinkedList<List<ColorValue>> colors = new LinkedList<>();
        for (int y = 0; y < HSB_VERTICES; y++) {
            float brightness = 1 - (y / (float) HSB_VERTICES);
            LinkedList<ColorValue> row = new LinkedList<>();
            for (int x = 0; x < HSB_VERTICES; x++) {
                float saturation = x / (float) HSB_VERTICES;
                row.add(ColorValue.create(Color.HSBtoRGB(hue, saturation, brightness)));
            }
            colors.add(row);
        }
        return createColorMatrix(x1, y1, x2, y2, colors);
    }

    @SuppressWarnings("SameParameterValue")
    private static List<VertexInfo> createHuePanel(int x1, int y1, int x2, int y2) {
        LinkedList<List<ColorValue>> colors = new LinkedList<>();
        for (int x = 0; x < HUE_VERTICES; x++) {
            float hue = x / (float) HUE_VERTICES;
            ColorValue color = ColorValue.create(Color.HSBtoRGB(hue, 1, 1));
            colors.add(List.of(color, color));
        }
        return createColorMatrix(x1, y1, x2, y2, colors);
    }

    private static List<VertexInfo> createColorMatrix(int x1, int y1, int x2, int y2, List<List<ColorValue>> list) {
        ImmutableList.Builder<VertexInfo> vertices = ImmutableList.builder();
        for (int i = 0; i < list.size() - 1; i++) {
            float ty1 = i / (float) (list.size() - 1);
            float ty2 = (i + 1) / (float) (list.size() - 1);
            for (int j = 0; j < list.get(i).size() - 1; j++) {
                float tx1 = j / (float) (list.get(i).size() - 1);
                float tx2 = (j + 1) / (float) (list.get(i).size() - 1);

                float cx1 = Mth.lerp(tx1, x1, x2);
                float cx2 = Mth.lerp(tx2, x1, x2);
                float cy1 = Mth.lerp(ty1, y1, y2);
                float cy2 = Mth.lerp(ty2, y1, y2);

                vertices.add(new VertexInfo(cx1, cy1, list.get(i).get(j)));
                vertices.add(new VertexInfo(cx1, cy2, list.get(i + 1).get(j)));
                vertices.add(new VertexInfo(cx2, cy2, list.get(i + 1).get(j + 1)));
                vertices.add(new VertexInfo(cx2, cy1, list.get(i).get(j + 1)));
            }
        }
        return vertices.build();
    }

    private static class ValueSlider extends AbstractSliderButton {

        private final String translationKey;
        private final Supplier<Integer> getter;
        private final Consumer<Integer> setter;

        public ValueSlider(int x, int y, int width, int height, String translationKey, Supplier<Integer> getter, Consumer<Integer> setter) {
            super(x, y, width, height, Component.empty(), Mth.clamp(getter.get(), 0, 255) / 255d);
            this.translationKey = translationKey;
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.translatable(this.translationKey, this.getter.get()));
        }

        @Override
        protected void applyValue() {
            this.setter.accept(Mth.clamp((int) Math.round(this.value * 255), 0, 255));
        }

        public void update() {
            this.value = Mth.clamp(this.getter.get(), 0, 255) / 255d;
            this.updateMessage();
        }
    }
}

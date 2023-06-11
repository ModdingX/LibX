package org.moddingx.libx.impl.config.gui.editor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.InputProperties;
import org.moddingx.libx.config.gui.WidgetProperties;
import org.moddingx.libx.config.validator.ValidatorInfo;
import org.moddingx.libx.render.RenderHelper;

import javax.annotation.Nonnull;
import java.util.Optional;

public class InputEditor<T> implements ConfigEditor<T> {

    private final InputProperties<T> properties;
    private final ValidatorInfo<?> validator;

    public InputEditor(InputProperties<T> properties, ValidatorInfo<?> validator) {
        this.properties = properties;
        this.validator = validator;
    }

    @Override
    public T defaultValue() {
        return this.properties.defaultValue();
    }

    @Override
    public AbstractWidget createWidget(Screen screen, T initialValue, WidgetProperties<T> properties) {
        return new InputWidget<>(this.properties, this.validator, initialValue, properties);
    }

    @Override
    public AbstractWidget updateWidget(Screen screen, AbstractWidget old, WidgetProperties<T> properties) {
        if (old instanceof InputWidget) {
            //noinspection unchecked
            return new InputWidget<>(this.properties, this.validator, (InputWidget<T>) old, properties);
        } else {
            return this.createWidget(screen, this.defaultValue(), properties);
        }
    }

    public static class InputWidget<T> extends EditBox {

        private final InputProperties<T> input;
        private final ValidatorInfo<?> validator;
        private String last = null;

        private InputWidget(InputProperties<T> input, ValidatorInfo<?> validator, WidgetProperties<T> properties, String initialValue) {
            super(Minecraft.getInstance().font, properties.x(), properties.y(), properties.width(), properties.height(), Component.empty());
            this.input = input;
            this.validator = validator;
            this.setMaxLength(32767);
            this.setValue(initialValue);
            this.setFilter(str -> {
                for (char chr : str.toCharArray()) {
                    if (!this.input.canInputChar(chr)) {
                        return false;
                    }
                }
                return true;
            });
            this.setResponder(str -> {
                if (this.last == null || !this.last.equals(str)) {
                    this.last = str;
                    if (this.input.isValid(str)) {
                        // No need to check validator here, will be validated when saving
                        properties.inputChanged().accept(this.input.valueOf(str));
                    }
                }
            });
        }

        public InputWidget(InputProperties<T> input, ValidatorInfo<?> validator, T initialValue, WidgetProperties<T> properties) {
            this(input, validator, properties, input.toString(initialValue));
        }

        public InputWidget(InputProperties<T> input, ValidatorInfo<?> validator, InputWidget<T> old, WidgetProperties<T> properties) {
            this(input, validator, properties, old.getValue());
        }

        // Used internally by other config editors
        // may not check `last`
        public Optional<T> getValidInput() {
            return this.getValidInput(this.getValue());
        }
        
        private Optional<T> getValidInput(String str) {
            if (this.input.isValid(str)) {
                T t = this.input.valueOf(str);
                if (this.validator.isValid(t)) {
                    return Optional.of(t);
                }
            }
            return Optional.empty();
        }

        @Override
        public void renderWidget(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.renderWidget(graphics, mouseX, mouseY, partialTicks);
            if (this.isVisible() && this.getValidInput(this.getValue()).isEmpty()) {
                graphics.pose().pushPose();
                graphics.pose().translate(0, 0, 10);
                RenderHelper.rgb(0xFF2222);
                graphics.blit(RenderHelper.TEXTURE_WHITE, this.getX() - 1, this.getY() - 1, 0, 0, this.width + 2, 1, 256, 256);
                graphics.blit(RenderHelper.TEXTURE_WHITE, this.getX() - 1, this.getY() + this.height, 0, 0, this.width + 2, 1, 256, 256);
                graphics.blit(RenderHelper.TEXTURE_WHITE, this.getX() - 1, this.getY() - 1, 0, 0, 1, this.height + 2, 256, 256);
                graphics.blit(RenderHelper.TEXTURE_WHITE, this.getX() + this.width, this.getY() - 1, 0, 0, 1, this.height + 2, 256, 256);
                RenderHelper.resetColor();
                graphics.pose().popPose();
            }
        }
    }
}

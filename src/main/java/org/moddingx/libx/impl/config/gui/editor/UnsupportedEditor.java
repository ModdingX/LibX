package org.moddingx.libx.impl.config.gui.editor;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.EditorOps;
import org.moddingx.libx.config.gui.WidgetProperties;

public class UnsupportedEditor<T> implements ConfigEditor<T> {
    
    private final T defaultValue;

    public UnsupportedEditor(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public T defaultValue() {
        return this.defaultValue;
    }

    @Override
    public AbstractWidget createWidget(Screen screen, T initialValue, WidgetProperties<T> properties) {
        // initialValue is null for the unsupported editor
        return this.create(properties);
    }

    @Override
    public AbstractWidget updateWidget(Screen screen, AbstractWidget old, WidgetProperties<T> properties) {
        return this.create(properties);
    }
    
    private AbstractWidget create(WidgetProperties<T> properties) {
        return new UnsupportedButton(properties.x(), properties.y(), properties.width(), properties.height(), Component.translatable("libx.config.editor.unsupported.title").withStyle(ChatFormatting.RED), Tooltip.create(Component.translatable("libx.config.editor.unsupported.description")), b -> {});
    }
    
    // Required so it's not possible to enable the button via EditorOps
    private static class UnsupportedButton extends Button implements EditorOps {

        public UnsupportedButton(int x, int y, int width, int height, Component title, Tooltip tooltip, OnPress action) {
            super(Button.builder(title, action)
                    .pos(x, y)
                    .size(width, height));
            this.setTooltip(tooltip);
            this.active = false;
        }

        @Override
        public void enabled(boolean enabled) {
            //
        }
    }
}

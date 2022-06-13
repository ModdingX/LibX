package org.moddingx.libx.impl.config.gui.editor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.EditorOps;
import org.moddingx.libx.config.gui.WidgetProperties;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

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
        return this.create(screen, properties);
    }

    @Override
    public AbstractWidget updateWidget(Screen screen, AbstractWidget old, WidgetProperties<T> properties) {
        return this.create(screen, properties);
    }
    
    private AbstractWidget create(Screen screen, WidgetProperties<T> properties) {
        Button button = new UnsupportedButton(properties.x(), properties.y(), properties.width(), properties.height(), Component.translatable("libx.config.editor.unsupported.title").withStyle(ChatFormatting.RED), b -> {}) {
            @Override
            public void renderToolTip(@Nonnull PoseStack poseStack, int mouseX, int mouseY) {
                screen.renderTooltip(poseStack, List.of(Component.translatable("libx.config.editor.unsupported.description")), Optional.empty(), mouseX, mouseY);
            }
        };
        button.active = false;
        return button;
    }
    
    // Required so it's not possible to enable the button via EditorOps
    private static class UnsupportedButton extends Button implements EditorOps {

        public UnsupportedButton(int x, int y, int width, int height, Component title, OnPress action) {
            super(x, y, width, height, title, action);
        }

        @Override
        public void enabled(boolean enabled) {
            //
        }
    }
}

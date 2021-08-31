package io.github.noeppi_noeppi.libx.impl.config.gui;

import io.github.noeppi_noeppi.libx.config.gui.ConfigEditor;
import io.github.noeppi_noeppi.libx.config.gui.WidgetProperties;
import io.github.noeppi_noeppi.libx.impl.config.gui.editor.InternalAwareEditor;
import io.github.noeppi_noeppi.libx.impl.config.gui.editor.UnsupportedEditor;
import io.github.noeppi_noeppi.libx.impl.config.gui.screen.ConfigBaseScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;

public class EditorHelper {
    
    public static <T> AbstractWidget create(Screen screen, ConfigEditor<T> editor, @Nullable T initialValue, @Nullable AbstractWidget old, WidgetProperties<T> properties) {
        ConfigEditor<T> resolved = resolveEditor(editor, screen);
        if (old == null) {
            return resolved.createWidget(screen, initialValue == null ? resolved.defaultValue() : initialValue, properties);
        } else {
            return resolved.updateWidget(screen, old, properties);
        }
    }
    
    private static <T> ConfigEditor<T> resolveEditor(ConfigEditor<T> editor, Screen screen) {
        if (editor instanceof InternalAwareEditor<T> internal && screen instanceof ConfigBaseScreen base && base.getCurrentManager() != null) {             
            return resolveEditor(internal.withManager(base.getCurrentManager()), screen);
        } else {
            return editor;
        }
    }
}

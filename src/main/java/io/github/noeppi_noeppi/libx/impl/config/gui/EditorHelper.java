package io.github.noeppi_noeppi.libx.impl.config.gui;

import io.github.noeppi_noeppi.libx.config.gui.ConfigEditor;
import io.github.noeppi_noeppi.libx.config.gui.WidgetProperties;
import io.github.noeppi_noeppi.libx.impl.config.gui.screen.ConfigBaseScreen;
import io.github.noeppi_noeppi.libx.impl.config.gui.screen.ConfigScreenManager;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;

public class EditorHelper {
    
    public static <T> AbstractWidget create(Screen screen, ConfigEditor<T> editor, @Nullable T initialValue, @Nullable AbstractWidget old, WidgetProperties<T> properties) {
        if (old == null) {
            return editor.createWidget(screen, initialValue == null ? editor.defaultValue() : initialValue, properties);
        } else {
            return editor.updateWidget(screen, old, properties);
        }
    }
    
    @Nullable
    public static ConfigScreenManager getManager(Screen screen) {
        if (screen instanceof ConfigBaseScreen base && base.getCurrentManager() != null) {             
            return base.getCurrentManager();
        } else {
            return null;
        }
    }
}

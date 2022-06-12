package org.moddingx.libx.impl.config.gui;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.WidgetProperties;
import org.moddingx.libx.impl.config.gui.screen.ConfigBaseScreen;
import org.moddingx.libx.impl.config.gui.screen.ConfigScreenManager;

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

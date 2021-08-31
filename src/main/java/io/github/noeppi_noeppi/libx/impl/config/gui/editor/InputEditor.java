package io.github.noeppi_noeppi.libx.impl.config.gui.editor;

import io.github.noeppi_noeppi.libx.config.gui.ConfigEditor;
import io.github.noeppi_noeppi.libx.config.gui.InputProperties;
import io.github.noeppi_noeppi.libx.config.gui.WidgetProperties;
import io.github.noeppi_noeppi.libx.screen.Panel;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

public class InputEditor<T> implements ConfigEditor<T> {

    private final InputProperties<T> properties;

    public InputEditor(InputProperties<T> properties) {
        this.properties = properties;
    }

    @Override
    public T defaultValue() {
        return this.properties.defaultValue();
    }

    @Override
    public AbstractWidget createWidget(Screen screen, T initialValue, WidgetProperties<T> properties) {
        return null;
    }

    @Override
    public AbstractWidget updateWidget(Screen screen, AbstractWidget old, WidgetProperties<T> properties) {
        return null;
    }

//    private static class OptionWidget<T> extends Panel {
//        
//    }
}

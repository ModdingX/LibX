package io.github.noeppi_noeppi.libx.config.gui;

import io.github.noeppi_noeppi.libx.config.ValidatorInfo;
import io.github.noeppi_noeppi.libx.impl.config.gui.editor.InputEditor;
import io.github.noeppi_noeppi.libx.impl.config.gui.editor.OptionEditor;
import io.github.noeppi_noeppi.libx.impl.config.gui.editor.UnsupportedEditor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface ConfigEditor<T> {
    
    T defaultValue();

    AbstractWidget createWidget(Screen screen, T initialValue, WidgetProperties<T> properties);
    AbstractWidget updateWidget(Screen screen, AbstractWidget old, WidgetProperties<T> properties);
    
    static <T> ConfigEditor<T> unsupported(T defaultValue) {
        return new UnsupportedEditor<>(defaultValue);
    }
    
    static <T> ConfigEditor<Optional<T>> option(ConfigEditor<T> editor) {
        return new OptionEditor<>(editor);
    }
    
    static <T> ConfigEditor<T> toggle(List<T> elems, Function<T, String> name) {
        throw new RuntimeException("Not implemented");
    }
    
    static ConfigEditor<String> input() {
        return input(InputProperties.PLAIN, ValidatorInfo.empty());
    }
    
    static ConfigEditor<String> input(ValidatorInfo<?> validator) {
        return input(InputProperties.PLAIN, validator);
    }
    
    static <T> ConfigEditor<T> input(InputProperties<T> input) {
        return input(input, ValidatorInfo.empty());
    }
    
    static <T> ConfigEditor<T> input(InputProperties<T> input, ValidatorInfo<?> validator) {
        return new InputEditor<>(input, validator);
    }
    
    static ConfigEditor<Double> slider() {
        throw new RuntimeException("Not implemented");
    }
    
    static <T> ConfigEditor<T> screen(Component title, List<ConfigEditorEntry> entries, Function<T, List<?>> valueFill) {
        throw new RuntimeException("Not implemented");
    }
    
    // Needed so we can still render own title, back button and such things and capture key events
    // The screen from the factory will be rendered into our own screen. Let's hope this works
    static <T> ConfigEditor<T> custom(Component title, ConfigScreenContent<T> content) {
        throw new RuntimeException("Not implemented");
    }
}

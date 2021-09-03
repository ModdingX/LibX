package io.github.noeppi_noeppi.libx.config.gui;

import io.github.noeppi_noeppi.libx.config.ValidatorInfo;
import io.github.noeppi_noeppi.libx.impl.config.gui.editor.*;
import io.github.noeppi_noeppi.libx.impl.config.gui.screen.content.SelectContent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

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
    
    static <T> ConfigEditor<T> toggle(List<T> elems) {
        return toggle(elems, e -> new TextComponent(e.toString()));
    }

    static <T> ConfigEditor<T> toggle(List<T> elems, Function<T, Component> name) {
        if (elems.size() <= 5) {
            return new SimpleSelectEditor<>(elems, name);
        } else {
            return custom(elems.get(0), current -> new SelectContent<>(elems, name, current));
        }
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
    
    // doubles between 0 and 1
    static <T> ConfigEditor<T> slider(Function<T, Double> extractor, Function<Double, T> factory) {
        return new SliderEditor<>(extractor, factory);
    }
    
    // Needed so we can still render own title, back button and such things and capture key events
    // The screen from the factory will be rendered into our own screen. Let's hope this works
    static <T> ConfigEditor<T> custom(T defaultValue, Function<T, ConfigScreenContent<T>> contentFactory) {
        return new CustomEditor<>(defaultValue, contentFactory);
    }
}

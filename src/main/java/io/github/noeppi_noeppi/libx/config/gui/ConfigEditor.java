package io.github.noeppi_noeppi.libx.config.gui;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ConfigEditor<T> {
    
    AbstractWidget createWidget(int x, int y, int width, int height, Consumer<T> inputChanged);
    
    default <U> ConfigEditor<U> map(Function<T, U> mapper) {
        return (x, y, width, height, inputChanged) -> this.createWidget(x, y, width, height, value -> {
            try {
                inputChanged.accept(mapper.apply(value));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    // Show a msg that a value can't be edited from the GUI and you need to edit the JSON directly
    static <T> ConfigEditor<T> unsupported() {
        throw new RuntimeException("Not implemented");
    }
    
    static <T> ConfigEditor<Optional<T>> option(ConfigEditor<T> editor) {
        throw new RuntimeException("Not implemented");
    }
    
    static <T> ConfigEditor<T> toggle(List<T> elems, Function<T, String> name, Consumer<T> inputChanged) {
        throw new RuntimeException("Not implemented");
    }
    
    static ConfigEditor<String> input(Consumer<String> inputChanged) {
        throw new RuntimeException("Not implemented");
    }
    
    static ConfigEditor<Double> slider(Consumer<Double> inputChanged) {
        throw new RuntimeException("Not implemented");
    }
    
    static <T> ConfigEditor<T> screen(String name, List<ConfigEditorEntry> entries, Function<T, List<?>> valueFill, Consumer<List<?>> inputChanged) {
        throw new RuntimeException("Not implemented");
    }
    
    // Needed so we can still render own title, back button and such things and capture key events
    // The screen from the factory will be rendered into our own screen. Let's hope this works
    static <T> ConfigEditor<T> custom(String name, Function<T, Screen> factory) {
        throw new RuntimeException("Not implemented");
    }
}

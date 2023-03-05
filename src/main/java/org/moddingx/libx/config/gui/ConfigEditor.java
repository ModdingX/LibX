package org.moddingx.libx.config.gui;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.moddingx.libx.config.validator.ValidatorInfo;
import org.moddingx.libx.impl.config.gui.editor.*;
import org.moddingx.libx.impl.config.gui.screen.content.SelectContent;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * A config editor provides the logic on how to display a config value in a GUI. The config
 * editor may not store additional state, as the same editor can be used for multiple entries.
 * However, the {@link AbstractWidget widgets} created by an editor can store state.
 * @param <T>
 */
public interface ConfigEditor<T> {

    /**
     * Gets the default value for places where elements with this editor are created that have
     * no value set.
     */
    T defaultValue();

    /**
     * Creates a widget for a given initial value.
     * 
     * @param screen The screen used for display. This screen <b>must</b> be used if the widget wants
     *               to draw tooltips, or they won't work correctly.
     * @param initialValue The initial value for the created widget.
     * @param properties Additional properties for the widget.
     */
    AbstractWidget createWidget(Screen screen, T initialValue, WidgetProperties<T> properties);

    /**
     * Updates a previously created widget, for example, after a screen resize.
     * 
     * @param screen The screen used for display. This screen <b>must</b> be used if the widget wants
     *               to draw tooltips, or they won't work correctly.
     * @param old The old widget that was created before, either by {@link #createWidget} or by this method.
     * @param properties Additional properties for the widget.
     */
    AbstractWidget updateWidget(Screen screen, AbstractWidget old, WidgetProperties<T> properties);

    /**
     * Creates a new editor that states, that a value can't be edited through the GUI.
     * 
     * @param defaultValue The value that should be used as the default value.
     */
    static <T> ConfigEditor<T> unsupported(T defaultValue) {
        return new UnsupportedEditor<>(defaultValue);
    }

    /**
     * Creates a new editor that represents an option of another editor by adding a checkbox to
     * select whether a value is set or not.
     */
    static <T> ConfigEditor<Optional<T>> option(ConfigEditor<T> editor) {
        return new OptionEditor<>(editor);
    }

    /**
     * Creates a toggle editor for a set of elements. This can be a single button that cycles on click
     * or a sub screen so select the element, depending on the number of elements given.
     */
    static <T> ConfigEditor<T> toggle(List<T> elems) {
        return toggle(elems, e -> Component.literal(e.toString()));
    }

    /**
     * Creates a toggle editor for a set of elements. This can be a single button that cycles on click
     * or a sub screen so select the element, depending on the number of elements given.
     * 
     * @param name A function that defines how to convert elements into a {@link Component} for display.
     */
    static <T> ConfigEditor<T> toggle(List<T> elems, Function<T, Component> name) {
        if (elems.size() <= 5) {
            return new SimpleSelectEditor<>(elems, name);
        } else {
            return custom(elems.get(0), current -> new SelectContent<>(elems, name, current));
        }
    }

    /**
     * Creates a new text input editor.
     */
    static ConfigEditor<String> input() {
        return input(InputProperties.PLAIN, ValidatorInfo.empty());
    }
    
    /**
     * Creates a new text input editor.
     * 
     * @param validator Validator information used. This causes the widget to go red when invalid values are entered.
     */
    static ConfigEditor<String> input(ValidatorInfo<?> validator) {
        return input(InputProperties.PLAIN, validator);
    }
    
    /**
     * Creates a new text input editor.
     * 
     * @param input Information on which input to allow and how to process that input.
     */
    static <T> ConfigEditor<T> input(InputProperties<T> input) {
        return input(input, ValidatorInfo.empty());
    }
    
    /**
     * Creates a new text input editor.
     * 
     * @param input Information on which input to allow and how to process that input.
     * @param validator Validator information used. This causes the widget to go red when invalid values are entered.         
     */
    static <T> ConfigEditor<T> input(InputProperties<T> input, ValidatorInfo<?> validator) {
        return new InputEditor<>(input, validator);
    }

    /**
     * Creates a new editor for a slider.
     * 
     * @param extractor A function that extracts a value from a {@link Double double} between 0 and 1
     * @param factory A function that turns a {@link Double double} between 0 and 1 into a value
     */
    static <T> ConfigEditor<T> slider(Function<T, Double> extractor, Function<Double, T> factory) {
        return new SliderEditor<>(extractor, factory);
    }

    /**
     * Creates a config editor with a button that will then open a sub-screen based on the
     * given {@link ConfigScreenContent content}.
     * 
     * @param contentFactory A function that creates a new screen content from a given initial value.
     */
    static <T> ConfigEditor<T> custom(T defaultValue, Function<T, ConfigScreenContent<T>> contentFactory) {
        return new CustomEditor<>(defaultValue, contentFactory);
    }
}

package org.moddingx.libx.impl.config.gui.editor;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.WidgetProperties;

import java.util.function.Consumer;
import java.util.function.Function;

public class SliderEditor<T> implements ConfigEditor<T> {

    private final Function<T, Double> extractor;
    private final Function<Double, T> factory;

    public SliderEditor(Function<T, Double> extractor, Function<Double, T> factory) {
        this.extractor = extractor;
        this.factory = factory;
    }

    @Override
    public T defaultValue() {
        return this.factory.apply(0d);
    }

    @Override
    public AbstractWidget createWidget(Screen screen, T initialValue, WidgetProperties<T> properties) {
        return new SliderWidget<>(this.factory, properties, initialValue, this.extractor.apply(initialValue));
    }

    @Override
    public AbstractWidget updateWidget(Screen screen, AbstractWidget old, WidgetProperties<T> properties) {
        if (old instanceof SliderWidget) {
            //noinspection unchecked
            return new SliderWidget<>(this.factory, properties, (SliderWidget<T>) old);
        } else {
            return this.createWidget(screen, this.defaultValue(), properties);
        }
    }

    private static class SliderWidget<T> extends AbstractSliderButton {

        private final Function<Double, T> factory;
        private final Consumer<T> inputChanged;
        private T current;

        public SliderWidget(Function<Double, T> factory, WidgetProperties<T> properties, SliderWidget<T> old) {
            this(factory, properties, old.current, old.value);
        }
        
        public SliderWidget(Function<Double, T> factory, WidgetProperties<T> properties, T current, double state) {
            super(properties.x(), properties.y(), properties.width(), properties.height(), Component.empty(), state);
            this.factory = factory;
            this.inputChanged = properties.inputChanged();
            this.current = current;
            this.value = Mth.clamp(state, 0, 1);
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal(this.current.toString()));
        }

        @Override
        protected void applyValue() {
            this.current = this.factory.apply(Mth.clamp(this.value, 0, 1));
            this.inputChanged.accept(this.current);
        }
    }
}

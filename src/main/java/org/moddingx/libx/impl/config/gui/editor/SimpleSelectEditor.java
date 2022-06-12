package org.moddingx.libx.impl.config.gui.editor;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.WidgetProperties;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class SimpleSelectEditor<T> implements ConfigEditor<T> {

    private final List<T> list;
    private final Function<T, Component> nameFactory;

    public SimpleSelectEditor(List<T> list, Function<T, Component> nameFactory) {
        this.list = ImmutableList.copyOf(list);
        if (this.list.isEmpty()) throw new IllegalArgumentException("Empty select config editor.");
        this.nameFactory = nameFactory;
    }

    @Override
    public T defaultValue() {
        return this.list.get(0);
    }

    @Override
    public AbstractWidget createWidget(Screen screen, T initialValue, WidgetProperties<T> properties) {
        int idx = Mth.clamp(this.list.indexOf(initialValue), 0, this.list.size() - 1);
        return new ToggleWidget<>(this.list, this.nameFactory, idx, properties);
    }

    @Override
    public AbstractWidget updateWidget(Screen screen, AbstractWidget oldWidget, WidgetProperties<T> properties) {
        if (oldWidget instanceof ToggleWidget<?> old) {
            return new ToggleWidget<>(this.list, this.nameFactory, old.getIdx(), properties);
        } else {
            return this.createWidget(screen, this.defaultValue(), properties);
        }
    }

    private static class ToggleWidget<T> extends Button {

        private final List<T> list;
        private final Function<T, Component> nameFactory;
        private final Consumer<T> inputChanged;
        private int idx;

        public ToggleWidget(List<T> list, Function<T, Component> nameFactory, int idx, WidgetProperties<T> properties) {
            super(properties.x(), properties.y(), properties.width(), properties.height(), new TextComponent(""), b -> {});
            this.list = list;
            this.nameFactory = nameFactory;
            this.inputChanged = properties.inputChanged();
            this.idx = idx;
            this.update();
        }

        @Override
        public void onPress() {
            this.idx += 1;
            this.inputChanged.accept(this.update());
        }
        
        private T update() {
            this.idx = (this.idx + this.list.size()) % this.list.size();
            T t = this.list.get(this.idx);
            this.setMessage(this.nameFactory.apply(t));
            return t;
        }

        public int getIdx() {
            return this.idx;
        }
    }
}

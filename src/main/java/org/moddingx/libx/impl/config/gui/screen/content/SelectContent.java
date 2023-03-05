package org.moddingx.libx.impl.config.gui.screen.content;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.moddingx.libx.config.gui.ConfigScreenContent;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

public class SelectContent<T> implements ConfigScreenContent<T> {

    private final List<T> list;
    private final Function<T, Component> nameFactory;
    private Consumer<T> inputChanged;
    private T current;

    public SelectContent(List<T> list, Function<T, Component> nameFactory, T current) {
        this.list = list;
        if (this.list.isEmpty()) throw new IllegalArgumentException("Empty select config editor.");
        this.nameFactory = nameFactory;
        if (this.list.contains(current)) {
            this.current = current;
        } else {
            this.current = this.list.get(0);
        }
    }

    @Override
    public Component title() {
        return Component.translatable("libx.config.gui.select.title");
    }

    @Override
    public Component message() {
        return this.nameFactory.apply(this.current);
    }

    @Override
    public boolean searchable() {
        return true;
    }

    @Override
    public void init(Consumer<T> inputChanged) {
        this.inputChanged = inputChanged;
    }

    @Override
    public void buildGui(Screen screen, ScreenManager manager, String search, Consumer<AbstractWidget> consumer) {
        int y = 0;
        int padding = Math.max(0, manager.contentWidth() - 200) / 2;
        for (T elem : this.list) {
            Component name = this.nameFactory.apply(elem);
            if (search.isBlank() || name.getString().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))) {
                Button button = Button.builder(name, b -> {
                            if (SelectContent.this.inputChanged != null) SelectContent.this.inputChanged.accept(elem);
                            SelectContent.this.current = elem;
                            manager.close();
                        })
                        .pos(padding, y)
                        .size(200, 20)
                        .build();
                consumer.accept(button);
                y += 23;
            }
        }
    }
}

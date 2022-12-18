package org.moddingx.libx.impl.config.gui.screen.content;

import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.ConfigScreenContent;
import org.moddingx.libx.config.gui.WidgetProperties;
import org.moddingx.libx.impl.config.gui.EditorHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

public class CollectionContent<T, C> implements ConfigScreenContent<C> {

    private final ConfigEditor<T> editor;
    private final Function<List<T>, C> factory;
    private final boolean canReorder;
    private Consumer<List<T>> inputChanged;
    private final List<T> list;
    private final List<AbstractWidget> widgets;

    public CollectionContent(List<T> value, ConfigEditor<T> editor, Function<List<T>, C> factory, boolean canReorder) {
        this.editor = editor;
        this.factory = factory;
        this.list = new ArrayList<>(value);
        this.canReorder = canReorder;
        //noinspection ConstantConditions
        this.widgets = new ArrayList<>(IntStream.range(0, this.list.size()).mapToObj(i -> (AbstractWidget) null).toList());
    }

    @Override
    public Component title() {
        return Component.translatable("libx.config.gui.list.title");
    }

    @Override
    public boolean searchable() {
        return false;
    }

    @Override
    public void init(Consumer<C> inputChanged) {
        this.inputChanged = (list) -> inputChanged.accept(this.factory.apply(list));
    }

    private void update() {
        if (this.inputChanged != null) {
            this.inputChanged.accept(ImmutableList.copyOf(this.list));
        }
    }
    
    @Override
    public void buildGui(Screen screen, ScreenManager manager, String search, Consumer<AbstractWidget> consumer) {
        int y = 0;
        for (int i = 0; i < this.list.size(); i++) {
            this.addEntryWidgets(screen, manager, consumer, i, y);
            y += 23;
        }

        int width = 200 + (23 * 3);
        int padding = Math.max(0, screen.width - width) / 2;
        Button button = Button.builder(Component.translatable("libx.config.gui.list.new"), b -> {
                    CollectionContent.this.list.add(CollectionContent.this.editor.defaultValue());
                    CollectionContent.this.widgets.add(null);
                    CollectionContent.this.update();
                    manager.rebuild();
                })
                .bounds(padding, y, 100, 20)
                .build();
        consumer.accept(button);
    }
    
    private void addEntryWidgets(Screen screen, ScreenManager manager, Consumer<AbstractWidget> consumer, int idx, int y) {
        int controlButtons = this.canReorder ? 3 : 1;
        int width = 200 + (23 * controlButtons);
        int padding = Math.max(0, screen.width - width) / 2;

        WidgetProperties<T> properties = new WidgetProperties<>(padding, y, 200, 20, t -> {
            this.list.set(idx, t);
            this.update();
        });
        AbstractWidget widget = EditorHelper.create(screen, this.editor, this.list.get(idx), this.widgets.get(idx), properties);
        this.widgets.set(idx, widget);
        consumer.accept(widget);

        if (this.canReorder) {
            addControlButton(consumer, padding + 203, y, Component.literal("\u2b06"), idx > 0, () -> {
                move(this.list, idx, idx - 1);
                move(this.widgets, idx, idx - 1);
                this.update();
                manager.rebuild();
            });

            addControlButton(consumer, padding + 226, y, Component.literal("\u2b07"), idx < this.list.size() - 1, () -> {
                move(this.list, idx, idx + 1);
                move(this.widgets, idx, idx + 1);
                this.update();
                manager.rebuild();
            });
        }

        int delButtonPadding = this.canReorder ? 249 : 203;
        addControlButton(consumer, padding + delButtonPadding, y, Component.literal("\u2716").withStyle(ChatFormatting.RED), true, () -> {
            this.list.remove(idx);
            this.widgets.remove(idx);
            this.update();
            manager.rebuild();
        });
    }
    
    public static void addControlButton(Consumer<AbstractWidget> consumer, int x, int y, Component text, boolean enable, Runnable action) {
        Button button = Button.builder(text, b -> action.run())
                .bounds(x, y, 20, 20)
                .build();
        button.active = enable;
        consumer.accept(button);
    }
    
    public static <T> void move(List<T> list, int from0, int to0) {
        int from = Mth.clamp(from0, 0, list.size() - 1);
        int to = Mth.clamp(to0, 0, list.size() - 1);
        T elem = list.remove(from);
        list.add(list.get(list.size() - 1));
        for (int i = list.size() - 2; i >= to && i > 0; i--) {
            list.set(i, list.get(i - 1));
        }
        list.set(to, elem);
    }
}

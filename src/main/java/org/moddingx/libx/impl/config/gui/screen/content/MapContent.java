package org.moddingx.libx.impl.config.gui.screen.content;

import com.google.common.collect.ImmutableMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.commons.lang3.tuple.Pair;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.ConfigScreenContent;
import org.moddingx.libx.config.gui.WidgetProperties;
import org.moddingx.libx.impl.config.gui.EditorHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class MapContent<T> implements ConfigScreenContent<Map<String, T>> {

    private final ConfigEditor<T> editor;
    private Consumer<Map<String, T>> inputChanged;
    private final List<Pair<String, T>> list;
    private final List<AbstractWidget> widgets;

    public MapContent(Map<String, T> value, ConfigEditor<T> editor) {
        this.editor = editor;
        this.list = new ArrayList<>(value.entrySet().stream()
                .map(e -> Pair.of(e.getKey(), e.getValue()))
                .sorted(Map.Entry.comparingByKey()).toList()
        );
        this.widgets = new ArrayList<>(IntStream.range(0, this.list.size()).mapToObj(i -> (AbstractWidget) null).toList());
    }

    @Override
    public Component title() {
        return new TranslatableComponent("libx.config.gui.map.title");
    }

    @Override
    public boolean searchable() {
        return false;
    }

    @Override
    public void init(Consumer<Map<String, T>> inputChanged) {
        this.inputChanged = inputChanged;
    }

    private void update() {
        if (this.inputChanged != null) {
            // No ImmutableMap builder in case the user enters duplicate keys
            Map<String, T> map = new HashMap<>();
            for (Pair<String, T> entry : this.list) {
                map.put(entry.getKey(), entry.getValue());
            }
            this.inputChanged.accept(ImmutableMap.copyOf(map));
        }
    }

    @Override
    public void buildGui(Screen screen, ScreenManager manager, String search, Consumer<AbstractWidget> consumer) {
        int y = 0;
        for (int i = 0; i < this.list.size(); i++) {
            this.addEntryWidgets(screen, manager, consumer, i, y);
            y += 23;
        }
        
        Button button = new Button(3, y, 100, 20, new TranslatableComponent("libx.config.gui.map.new"), b -> {}) {

            @Override
            public void onPress() {
                MapContent.this.list.add(Pair.of("", MapContent.this.editor.defaultValue()));
                MapContent.this.widgets.add(null);
                MapContent.this.update();
                manager.rebuild();
            }
        };
        consumer.accept(button);
    }
    
    private void addEntryWidgets(Screen screen, ScreenManager manager, Consumer<AbstractWidget> consumer, int idx, int y) {
        int width = Math.min(200, (screen.width - 64) / 2);

        AtomicReference<String> current = new AtomicReference<>(this.list.get(idx).getKey());
        EditBox keyInput = new EditBox(Minecraft.getInstance().font, 3, y, width, 20, new TextComponent(""));
        keyInput.setMaxLength(32767);
        keyInput.setValue(current.get());
        keyInput.setResponder(str -> {
            if (!current.get().equals(str)) {
                current.set(str);
                this.list.set(idx, Pair.of(str, this.list.get(idx).getValue()));
                this.update();
            }
        });
        consumer.accept(keyInput);
        
        WidgetProperties<T> properties = new WidgetProperties<>(screen.width - 31 - width, y, width, 20, t -> {
            this.list.set(idx, Pair.of(this.list.get(idx).getKey(), t));
            this.update();
        });
        AbstractWidget widget = EditorHelper.create(screen, this.editor, this.list.get(idx).getValue(), this.widgets.get(idx), properties);
        this.widgets.set(idx, widget);
        consumer.accept(widget);

        Button deleteEntryButton = new Button(screen.width - 28, y, 20, 20, new TextComponent("✖").withStyle(ChatFormatting.RED), b -> {}) {

            @Override
            public void onPress() {
                MapContent.this.list.remove(idx);
                MapContent.this.widgets.remove(idx);
                MapContent.this.update();
                manager.rebuild();
            }
        };
        consumer.accept(deleteEntryButton);
    }
}

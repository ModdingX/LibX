package io.github.noeppi_noeppi.libx.impl.config.gui.screen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.github.noeppi_noeppi.libx.impl.config.gui.ConfigDisplay;
import io.github.noeppi_noeppi.libx.impl.config.gui.screen.widget.TextWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class ConfigScreen<T> extends ConfigBaseScreen {

    public final ConfigDisplay display;
    public final ConfigScreenManager manager;
    protected final Map<BuiltCategory, List<T>> keys;
    protected final ElementFactory<T> factory;
    protected final SearchPredicate<T> searchPredicate;
    protected Map<T, BuiltEntry> elements = Map.of();

    public ConfigScreen(ConfigScreenManager manager, Component title, List<T> elements, ElementFactory<T> factory, SearchPredicate<T> searchPredicate) {
        this(manager, title, Map.of(BuiltCategory.EMPTY, elements), factory, searchPredicate);
    }
    
    public ConfigScreen(ConfigScreenManager manager, Component title, Map<BuiltCategory, List<T>> elements, ElementFactory<T> factory, SearchPredicate<T> searchPredicate) {
        super(title, manager, true);
        this.display = manager.display;
        this.manager = manager;
        //noinspection UnstableApiUsage
        this.keys = elements.entrySet().stream()
                .map(e -> Pair.of(e.getKey(), (List < T >) ImmutableList.copyOf(e.getValue())))
                .collect(ImmutableMap.toImmutableMap(Pair::getKey, Pair::getValue));
        this.factory = factory;
        this.searchPredicate = searchPredicate;
    }

    @Override
    protected void buildGui(Consumer<AbstractWidget> consumer) {
        int y = 5;
        int editorWidth = Math.min(200, (int) Math.round(this.width * (2 / 5d)));
        int titleWidth = Math.max(0, this.width - 15 - editorWidth - 25);
        ImmutableMap.Builder<T, BuiltEntry> entryBuilder = ImmutableMap.builder();
        String query = this.searchTerm();
        boolean first = true;
        for (BuiltCategory category : this.keys.keySet().stream().sorted(Comparator.comparing(BuiltCategory::id)).toList()) {
            if (this.keys.get(category).stream().anyMatch(t -> this.searchPredicate.test(t, this, query))) {
                if (!category.id().isEmpty()) {
                    if (!first) y+= 10;
                    consumer.accept(new TextWidget(this, 4, y, this.width - 6, 20, category.title(), category.description()));
                    y += 23;
                }
                first = false;
                for (T t : this.keys.get(category)) {
                    if (this.searchPredicate.test(t, this, query)) {
                        BuiltEntry oldEntry = this.elements.getOrDefault(t, null);
                        BuiltEntry entry = this.factory.create(t, this, oldEntry == null ? null : oldEntry.widget(), this.width - 5 - editorWidth, y, editorWidth, 20);
                        entryBuilder.put(t, entry);
                        consumer.accept(new TextWidget(this, 15, entry.widget().y, titleWidth, entry.widget().getHeight(), entry.title(), entry.description()));
                        consumer.accept(entry.widget());
                        y += 23;
                    }
                }
            }
        }
        this.elements = entryBuilder.build();
    }
    
    @Override
    protected void searchChange(String term) {
        this.rebuild();
    }

    public interface ElementFactory<T> {
        
        BuiltEntry create(T elem, ConfigScreen<T> screen, @Nullable AbstractWidget oldWidget, int x, int y, int width, int height);
    }

    public interface SearchPredicate<T> {
        
        boolean test(T elem, ConfigScreen<T> screen, String query);
    }
}

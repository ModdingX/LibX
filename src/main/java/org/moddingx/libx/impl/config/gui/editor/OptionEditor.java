package org.moddingx.libx.impl.config.gui.editor;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.EditorOps;
import org.moddingx.libx.config.gui.WidgetProperties;
import org.moddingx.libx.impl.config.gui.EditorHelper;
import org.moddingx.libx.screen.Panel;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

public class OptionEditor<T> implements ConfigEditor<Optional<T>> {

    private final ConfigEditor<T> editor;

    public OptionEditor(ConfigEditor<T> editor) {
        this.editor = editor;
    }

    @Override
    public Optional<T> defaultValue() {
        return Optional.empty();
    }

    @Override
    public AbstractWidget createWidget(Screen screen, Optional<T> initialValue, WidgetProperties<Optional<T>> properties) {
        return new OptionWidget<>(screen, this.editor, initialValue, properties);
    }

    @Override
    public AbstractWidget updateWidget(Screen screen, AbstractWidget old, WidgetProperties<Optional<T>> properties) {
        if (old instanceof OptionWidget) {
            //noinspection unchecked
            return new OptionWidget<>(screen, this.editor, (OptionWidget<T>) old, properties);
        } else {
            return this.createWidget(screen, this.defaultValue(), properties);
        }
    }

    private static class OptionWidget<T> extends Panel {

        private final Consumer<Optional<T>> inputChanged;
        private final Checkbox box;
        private final AbstractWidget widget;
        
        private T value;
        
        private OptionWidget(Screen screen, ConfigEditor<T> editor, WidgetProperties<Optional<T>> properties, T value, boolean selected, @Nullable AbstractWidget old) {
            super(screen, properties.x() - 22, properties.y(), properties.width() + 22, properties.height());

            this.inputChanged = properties.inputChanged();
            this.value = value;
            
            this.box = new Checkbox(0, 0, 20, Math.min(20, properties.height()), Component.empty(), selected, false) {
                @Override
                public void onPress() {
                    super.onPress();
                    EditorOps.wrap(OptionWidget.this.widget).enabled(this.selected());
                    OptionWidget.this.update(null);
                }
            };

            WidgetProperties<T> modified = new WidgetProperties<>(22, 0, properties.width(), properties.height(), this::update);
            this.widget = EditorHelper.create(screen, editor, this.value, old, modified);
            EditorOps.wrap(OptionWidget.this.widget).enabled(selected);

            this.addRenderableWidget(this.box);
            this.addRenderableWidget(this.widget);
        }
        
        public OptionWidget(Screen screen, ConfigEditor<T> editor, Optional<T> initialValue, WidgetProperties<Optional<T>> properties) {
            this(screen, editor, properties, initialValue.orElse(editor.defaultValue()), initialValue.isPresent(), null);
        }

        public OptionWidget(Screen screen, ConfigEditor<T> editor, OptionWidget<T> old, WidgetProperties<Optional<T>> properties) {
            this(screen, editor, properties, old.value, old.box.selected(), old.widget);
        }
        
        private void update(@Nullable T t) {
            if (t != null) {
                this.value = t;
            }
            this.inputChanged.accept(this.box.selected() ? Optional.of(this.value) : Optional.empty());
        }

        @Override
        public void enabled(boolean enabled) {
            EditorOps.wrap(this.box).enabled(enabled);
        }
    }
}

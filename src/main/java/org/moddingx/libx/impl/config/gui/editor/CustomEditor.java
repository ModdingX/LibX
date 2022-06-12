package org.moddingx.libx.impl.config.gui.editor;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.ConfigScreenContent;
import org.moddingx.libx.config.gui.WidgetProperties;
import org.moddingx.libx.impl.config.gui.EditorHelper;
import org.moddingx.libx.impl.config.gui.screen.ConfigScreenManager;
import org.moddingx.libx.impl.config.gui.screen.CustomConfigScreen;
import org.moddingx.libx.util.LazyValue;

import java.util.function.Function;

public class CustomEditor<T> implements ConfigEditor<T> {

    private final T defaultValue;
    private final Function<T, ConfigScreenContent<T>> contentFactory;
    private final LazyValue<ConfigEditor<T>> unsupported;

    public CustomEditor(T defaultValue, Function<T, ConfigScreenContent<T>> contentFactory) {
        this.defaultValue = defaultValue;
        this.contentFactory = contentFactory;
        this.unsupported = new LazyValue<>(() -> ConfigEditor.unsupported(this.defaultValue));
    }

    @Override
    public T defaultValue() {
        return this.defaultValue;
    }

    @Override
    public AbstractWidget createWidget(Screen screen, T initialValue, WidgetProperties<T> properties) {
        ConfigScreenManager manager = EditorHelper.getManager(screen);
        if (manager != null) {
            ConfigScreenContent<T> content = this.contentFactory.apply(initialValue);
            content.init(properties.inputChanged());
            return new CustomButton<>(manager, content, properties);
        } else {
            return this.unsupported.get().createWidget(screen, initialValue, properties);
        }
    }

    @Override
    public AbstractWidget updateWidget(Screen screen, AbstractWidget old, WidgetProperties<T> properties) {
        if (old instanceof CustomButton) {
            ConfigScreenManager manager = EditorHelper.getManager(screen);
            if (manager != null) {
                //noinspection unchecked
                return new CustomButton<>(manager, ((CustomButton<T>) old).content, properties);
            } else {
                return this.unsupported.get().updateWidget(screen, old, properties);
            }
        } else {
            return this.createWidget(screen, this.defaultValue(), properties);
        }
    }
    
    private static class CustomButton<T> extends Button {

        private final ConfigScreenManager manager;
        private final ConfigScreenContent<T> content;
        
        public CustomButton(ConfigScreenManager manager, ConfigScreenContent<T> content, WidgetProperties<T> properties) {
            super(properties.x(), properties.y(), properties.width(), properties.height(), content.message(), b -> {});
            this.manager = manager;
            this.content = content;
        }

        @Override
        public void onPress() {
            this.manager.open(new CustomConfigScreen<>(this.manager, this.content, () -> this.setMessage(this.content.message())));
        }
    }
}

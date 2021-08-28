package io.github.noeppi_noeppi.libx.impl.config.gui;

import com.google.common.collect.ImmutableMap;
import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.config.gui.ConfigEditor;
import io.github.noeppi_noeppi.libx.impl.config.ConfigImpl;
import io.github.noeppi_noeppi.libx.impl.config.ConfigKey;
import io.github.noeppi_noeppi.libx.impl.config.ConfigState;
import io.github.noeppi_noeppi.libx.util.CachedValue;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class ConfigDisplay {
    
    private final ConfigImpl config;
    private final Map<ConfigKey, DisplayValue<?>> values;
    private final CachedValue<ConfigState> cachedState;

    public ConfigDisplay(ConfigImpl config, ConfigState currentState, ConfigState defaultState) {
        this.config = config;
        this.values = new HashMap<>();
        for (ConfigKey key : config.keys.values()) {
            //noinspection unchecked
            this.values.put(key, new DisplayValue<>(key, (ConfigEditor<Object>) key.mapper.createEditor(), currentState.getValue(key), defaultState.getValue(key)));
        }
        this.cachedState = new CachedValue<>(this::stateInternal);
    }
    
    public ConfigState state() {
        return this.cachedState.get();
    }
    
    private ConfigState stateInternal() {
        ImmutableMap.Builder<ConfigKey, Object> map = ImmutableMap.builder();
        for (Map.Entry<ConfigKey, DisplayValue<?>> entry : this.values.entrySet()) {
            Object value = entry.getValue().getValue();
            if (entry.getKey().mapper.type().isAssignableFrom(value.getClass())) {
                map.put(entry.getKey(), value);
            } else {
                LibX.logger.warn("Failed to create config state from user input: Editor produced invalid type. Expected: " + entry.getKey().mapper.type() + ", Got: " + value.getClass() + ", using fallback.");
                map.put(entry.getKey(), entry.getValue().defaultValue);
            }
        }
        return new ConfigState(this.config, map.build());
    }
    
    public AbstractWidget createWidget(ConfigKey key, Screen screen, int x, int y, int width, int height) {
        DisplayValue<?> value = this.values.get(key);
        if (value == null) {
            // Dummy
            return new AbstractWidget(x, y, width, height, new TextComponent("DUMMY")) {
                @Override
                public void updateNarration(@Nonnull NarrationElementOutput narration) {
                    //
                }
            };
        } else {
            //noinspection unchecked
            return value.editor.createWidget(screen, x, y, width, height, ((DisplayValue<Object>) value)::setValue);
        }
    }
    
    public void save() {
        this.config.applyInGameChanges(this.state());
    }

    private final class DisplayValue<T> {

        public final ConfigKey key;
        public final ConfigEditor<T> editor;
        public final T defaultValue;
        
        private T value;

        private DisplayValue(ConfigKey key, ConfigEditor<T> editor, T value, T defaultValue) {
            this.key = key;
            this.editor = editor;
            this.defaultValue = defaultValue;
            this.value = value;
        }

        public T getValue() {
            return this.value;
        }

        public void setValue(T value) {
            if (!this.key.mapper.type().isAssignableFrom(value.getClass())) {
                LibX.logger.warn("Failed to store config value from user input: Editor produced invalid type. Expected: " + this.key.mapper.type() + ", Got: " + value.getClass() + ", ignoring.");
            } else {
                if (this.value != value) {
                    this.value = value;
                    ConfigDisplay.this.cachedState.invalidate();
                }
            }
        }
    }
}

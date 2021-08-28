package io.github.noeppi_noeppi.libx.impl.config.gui;

import com.google.common.collect.ImmutableMap;
import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.config.gui.ConfigEditor;
import io.github.noeppi_noeppi.libx.impl.config.ConfigImpl;
import io.github.noeppi_noeppi.libx.impl.config.ConfigKey;
import io.github.noeppi_noeppi.libx.impl.config.ConfigState;

import java.util.HashMap;
import java.util.Map;

public class ConfigDisplay {
    
    private final ConfigImpl config;
    private final Map<ConfigKey, DisplayValue<?>> values;

    public ConfigDisplay(ConfigImpl config, ConfigState currentState, ConfigState defaultState) {
        this.config = config;
        this.values = new HashMap<>();
        for (ConfigKey key : config.keys.values()) {
            //noinspection unchecked
            this.values.put(key, new DisplayValue<>((ConfigEditor<Object>) key.mapper.createEditor(), currentState.getValue(key), defaultState.getValue(key)));
        }
    }
    
    public ConfigState state() {
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

    private static final class DisplayValue<T> {

        public final ConfigEditor<T> editor;
        public final T defaultValue;
        
        private T value;

        private DisplayValue(ConfigEditor<T> editor, T value, T defaultValue) {
            this.editor = editor;
            this.defaultValue = defaultValue;
            this.value = value;
        }

        public T getValue() {
            return this.value;
        }

        public void setValue(T value) {
            this.value = value;
        }
    }
}

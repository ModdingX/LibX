package org.moddingx.libx.impl.config.gui;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import org.moddingx.libx.LibX;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.WidgetProperties;
import org.moddingx.libx.impl.config.ConfigImpl;
import org.moddingx.libx.impl.config.ConfigKey;
import org.moddingx.libx.impl.config.ConfigState;
import org.moddingx.libx.util.CachedValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
            this.values.put(key, new DisplayValue<>(key, (ConfigEditor<Object>) key.mapper.createEditor(key.validatorAccess()), currentState.getValue(key), defaultState.getValue(key)));
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
    
    public AbstractWidget createWidget(ConfigKey key, Screen screen, @Nullable AbstractWidget old, int x, int y, int width, int height) {
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
            return EditorHelper.create(screen, ((ConfigEditor<Object>) value.editor), value.getValue(), old, new WidgetProperties<>(x, y, width, height, ((DisplayValue<Object>) value)::setValue));
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
                    //noinspection unchecked
                    this.value = (T) this.key.validate(value, "Invalid value in editor config", null);
                    ConfigDisplay.this.cachedState.invalidate();
                }
            }
        }
    }
}

package org.moddingx.libx.impl.config.gui.editor;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.WidgetProperties;
import org.moddingx.libx.config.validator.ValidatorInfo;
import org.moddingx.libx.impl.config.gui.EditorHelper;
import org.moddingx.libx.impl.config.gui.screen.ConfigScreenManager;
import org.moddingx.libx.impl.config.gui.screen.RecordConfigScreen;
import org.moddingx.libx.impl.config.mappers.special.RecordValueMapper;
import org.moddingx.libx.util.lazy.LazyValue;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RecordEditor<T extends Record> implements ConfigEditor<T> {

    private final Class<T> clazz;
    private final List<Supplier<ConfigEditor<Object>>> editors;
    private final Constructor<T> ctor;
    
    private final LazyValue<T> defaultValue;
    private final LazyValue<ConfigEditor<T>> unsupported;

    public RecordEditor(Class<T> clazz, List<RecordValueMapper.EntryData> entries, Constructor<T> ctor) {
        this.clazz = clazz;
        this.editors = entries.stream().map(entry -> (Supplier<ConfigEditor<Object>>) () -> {
            ValidatorInfo<?> validator = entry.validator() == null ? ValidatorInfo.empty() : entry.validator().access();
            return entry.mapper().createEditor(validator);
        }).toList();
        this.ctor = ctor;
        
        this.defaultValue = new LazyValue<>(() -> {
            RecordComponent[] parts = this.clazz.getRecordComponents();
            Object[] values = new Object[parts.length];
            for (int i = 0; i < parts.length; i++) {
                values[i] = this.editors.get(i).get().defaultValue();
            }
            try {
                return this.ctor.newInstance(values);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to create record for config editor.", e);
            }
        });
        
        this.unsupported = new LazyValue<>(() -> ConfigEditor.unsupported(this.defaultValue.get()));
    }

    @Override
    public T defaultValue() {
        return this.defaultValue.get();
    }

    @Override
    public AbstractWidget createWidget(Screen screen, T initialValue, WidgetProperties<T> properties) {
        ConfigScreenManager manager = EditorHelper.getManager(screen);
        if (manager != null) {
            ImmutableList.Builder<Object> values = ImmutableList.builder();
            RecordComponent[] parts = this.clazz.getRecordComponents();
            try {
                for (RecordComponent part : parts) {
                    values.add(RecordValueMapper.accessComponent(part, initialValue));
                }
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
                return this.unsupported.get().createWidget(screen, initialValue, properties);
            }
            return new RecordButton<>(manager, this.clazz, this.editors, this.ctor, values.build(), properties);
        } else {
            return this.unsupported.get().createWidget(screen, initialValue, properties);
        }
    }

    @Override
    public AbstractWidget updateWidget(Screen screen, AbstractWidget old, WidgetProperties<T> properties) {
        if (old instanceof RecordButton) {
            ConfigScreenManager manager = EditorHelper.getManager(screen);
            if (manager != null) {
                //noinspection unchecked
                return new RecordButton<>(manager, this.clazz, this.editors, this.ctor, ((RecordButton<T>) old).values, properties);
            } else {
                return this.unsupported.get().updateWidget(screen, old, properties);
            }
        } else {
            return this.createWidget(screen, this.defaultValue(), properties);
        }
    }
    
    private static class RecordButton<T> extends Button {
        
        private final ConfigScreenManager manager;
        private final Class<T> clazz;
        private final Constructor<T> ctor;
        private final List<Object> values;
        private final Consumer<T> inputChanged;
        
        private final LazyValue<List<RecordConfigScreen.Entry>> entries;

        public RecordButton(ConfigScreenManager manager, Class<T> clazz, List<Supplier<ConfigEditor<Object>>> editors, Constructor<T> ctor, List<Object> values, WidgetProperties<T> properties) {
            super(Button.builder(Component.translatable("libx.config.gui.edit"), b -> {})
                    .pos(properties.x(), properties.y())
                    .size(properties.width(), properties.height()));
            this.manager = manager;
            this.clazz = clazz;
            this.ctor = ctor;
            if (values.size() != clazz.getRecordComponents().length) throw new IllegalStateException("Record value size mismatch.");
            this.values = new ArrayList<>(values);
            this.inputChanged = properties.inputChanged();
            
            this.entries = new LazyValue<>(() -> {
                ImmutableList.Builder<RecordConfigScreen.Entry> entries = ImmutableList.builder();
                RecordComponent[] components = clazz.getRecordComponents();
                for (int i = 0; i < components.length; i++) {
                    int idx = i;
                    entries.add(new RecordConfigScreen.Entry(
                            components[idx], editors.get(idx),
                            () -> this.values.get(idx),
                            e -> this.update(idx, e)
                    ));
                }
                return entries.build();
            });
        }
        
        @Override
        public void onPress() {
            this.manager.open(new RecordConfigScreen(this.manager, Component.literal(this.clazz.getSimpleName()), this.entries.get()));
        }
        
        private void update(int idx, Object value) {
            if (idx >= 0 && idx < this.values.size()) {
                this.values.set(idx, value);
                try {
                    this.inputChanged.accept(this.ctor.newInstance(this.values.toArray()));
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

package io.github.noeppi_noeppi.libx.impl.config.gui.screen.content.component.type;

import io.github.noeppi_noeppi.libx.config.gui.ConfigEditor;
import io.github.noeppi_noeppi.libx.config.gui.ConfigScreenContent;
import io.github.noeppi_noeppi.libx.config.gui.WidgetProperties;
import io.github.noeppi_noeppi.libx.impl.config.gui.EditorHelper;
import io.github.noeppi_noeppi.libx.impl.config.gui.screen.content.component.ComponentType;
import io.github.noeppi_noeppi.libx.util.CachedValue;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.KeybindComponent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class KeybindComponentType implements ComponentType<KeybindComponent> {

    // Keybinds may change so we only make it a cached value
    // that can be updated when required
    private final CachedValue<ConfigEditor<String>> keyEditor;
    
    private String key = "";
    private AbstractWidget keyWidget;
    
    private Consumer<KeybindComponent> inputChanged;

    public KeybindComponentType() {
        this.keyEditor = new CachedValue<>(() -> ConfigEditor.toggle(KeyMapping.ALL.keySet().stream().sorted().toList()));
    }

    @Override
    public Component name() {
        return new TranslatableComponent("libx.config.gui.component.type_key");
    }

    @Override
    public KeybindComponent defaultValue() {
        if (KeyMapping.ALL.isEmpty()) {
            return new KeybindComponent("");
        } else {
            return new KeybindComponent(KeyMapping.ALL.keySet().stream().sorted().findFirst().get());
        }
    }

    @Nullable
    @Override
    public MutableComponent init(Component component, Consumer<KeybindComponent> inputChanged) {
        this.inputChanged = inputChanged;
        if (component instanceof KeybindComponent kc) {
            this.key = kc.getName();
            this.keyEditor.invalidate();
            return kc.plainCopy();
        } else {
            return null;
        }
    }

    @Override
    public void buildGui(Screen screen, ConfigScreenContent.ScreenManager manager, AtomicInteger y, Consumer<AbstractWidget> consumer) {
        int width = Math.min(200, screen.width - 10);
        WidgetProperties<String> properties = new WidgetProperties<>((screen.width - width) / 2, y.get(), width, 20, key -> {
            this.key = key;
            if (this.inputChanged != null) {
                this.inputChanged.accept(new KeybindComponent(key));
            }
        });
        this.keyWidget = EditorHelper.create(screen, this.keyEditor.get(), this.key, this.keyWidget, properties);
        consumer.accept(this.keyWidget);
        y.addAndGet(23);
    }
}

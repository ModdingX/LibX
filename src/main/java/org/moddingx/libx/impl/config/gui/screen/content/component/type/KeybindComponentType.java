package org.moddingx.libx.impl.config.gui.screen.content.component.type;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.KeybindContents;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.ConfigScreenContent;
import org.moddingx.libx.config.gui.WidgetProperties;
import org.moddingx.libx.impl.config.gui.EditorHelper;
import org.moddingx.libx.impl.config.gui.screen.content.component.ComponentType;
import org.moddingx.libx.util.CachedValue;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class KeybindComponentType implements ComponentType {

    // Keybinds may change so we only make it a cached value
    // that can be updated when required
    private final CachedValue<ConfigEditor<String>> keyEditor;

    private String key = "";
    private AbstractWidget keyWidget;

    private Consumer<MutableComponent> inputChanged;

    public KeybindComponentType() {
        this.keyEditor = new CachedValue<>(() -> ConfigEditor.toggle(KeyMapping.ALL.keySet().stream().sorted().toList()));
    }

    @Override
    public Component name() {
        return Component.translatable("libx.config.gui.component.type_key");
    }

    @Override
    public MutableComponent defaultValue() {
        if (KeyMapping.ALL.isEmpty()) {
            return Component.keybind("");
        } else {
            return Component.keybind(KeyMapping.ALL.keySet().stream().sorted().findFirst().get());
        }
    }

    @Nullable
    @Override
    public MutableComponent init(Component component, Consumer<MutableComponent> inputChanged) {
        this.inputChanged = inputChanged;
        if (component.getContents() instanceof KeybindContents kc) {
            this.key = kc.getName();
            this.keyEditor.invalidate();
            return component.plainCopy();
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
                this.inputChanged.accept(Component.keybind(key));
            }
        });
        this.keyWidget = EditorHelper.create(screen, this.keyEditor.get(), this.key, this.keyWidget, properties);
        consumer.accept(this.keyWidget);
        y.addAndGet(23);
    }
}

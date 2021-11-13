package io.github.noeppi_noeppi.libx.impl.config.gui.screen.content.component.type;

import io.github.noeppi_noeppi.libx.config.gui.ConfigEditor;
import io.github.noeppi_noeppi.libx.config.gui.ConfigScreenContent;
import io.github.noeppi_noeppi.libx.config.gui.WidgetProperties;
import io.github.noeppi_noeppi.libx.impl.config.gui.EditorHelper;
import io.github.noeppi_noeppi.libx.impl.config.gui.screen.content.component.ComponentType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class TextComponentType implements ComponentType<TextComponent> {
    
    private final ConfigEditor<String> editor;
    
    private String value = "";
    private AbstractWidget widget;
    
    private Consumer<TextComponent> inputChanged;

    public TextComponentType() {
        this.editor = ConfigEditor.input();
    }

    @Override
    public Component name() {
        return new TranslatableComponent("libx.config.gui.component.type_text");
    }

    @Override
    public TextComponent defaultValue() {
        return new TextComponent("");
    }

    @Nullable
    @Override
    public MutableComponent init(Component component, Consumer<TextComponent> inputChanged) {
        this.inputChanged = inputChanged;
        if (component instanceof TextComponent tc) {
            this.value = tc.getText();
            return tc.plainCopy();
        } else {
            return null;
        }
    }

    @Override
    public void buildGui(Screen screen, ConfigScreenContent.ScreenManager manager, AtomicInteger y, Consumer<AbstractWidget> consumer) {
        int width = screen.width - 10;
        WidgetProperties<String> properties = new WidgetProperties<>((screen.width - width) / 2, y.get(), width, 20, value -> {
            this.value = value;
            if (this.inputChanged != null) {
                this.inputChanged.accept(new TextComponent(value));
            }
        });
        this.widget = EditorHelper.create(screen, this.editor, this.value, this.widget, properties);
        consumer.accept(this.widget);
        y.addAndGet(23);
    }
}

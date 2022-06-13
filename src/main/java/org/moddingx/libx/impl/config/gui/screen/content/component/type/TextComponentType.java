package org.moddingx.libx.impl.config.gui.screen.content.component.type;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.ConfigScreenContent;
import org.moddingx.libx.config.gui.WidgetProperties;
import org.moddingx.libx.impl.config.gui.EditorHelper;
import org.moddingx.libx.impl.config.gui.screen.content.component.ComponentType;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class TextComponentType implements ComponentType {

    private final ConfigEditor<String> editor;

    private String value = "";
    private AbstractWidget widget;

    private Consumer<MutableComponent> inputChanged;

    public TextComponentType() {
        this.editor = ConfigEditor.input();
    }

    @Override
    public Component name() {
        return Component.translatable("libx.config.gui.component.type_text");
    }

    @Override
    public MutableComponent defaultValue() {
        return Component.literal("");
    }

    @Nullable
    @Override
    public MutableComponent init(Component component, Consumer<MutableComponent> inputChanged) {
        this.inputChanged = inputChanged;
        if (component.getContents() == ComponentContents.EMPTY) {
            this.value = "";
            return component.plainCopy();
        } else if (component.getContents() instanceof LiteralContents lc) {
            this.value = lc.text();
            return component.plainCopy();
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
                this.inputChanged.accept(Component.literal(value));
            }
        });
        this.widget = EditorHelper.create(screen, this.editor, this.value, this.widget, properties);
        consumer.accept(this.widget);
        y.addAndGet(23);
    }
}

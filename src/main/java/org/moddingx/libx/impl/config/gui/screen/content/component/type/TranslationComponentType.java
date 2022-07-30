package org.moddingx.libx.impl.config.gui.screen.content.component.type;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.ConfigScreenContent;
import org.moddingx.libx.config.gui.WidgetProperties;
import org.moddingx.libx.impl.config.gui.EditorHelper;
import org.moddingx.libx.impl.config.gui.screen.content.CollectionContent;
import org.moddingx.libx.impl.config.gui.screen.content.component.ComponentContent;
import org.moddingx.libx.impl.config.gui.screen.content.component.ComponentType;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Formattable;
import java.util.Formatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public class TranslationComponentType implements ComponentType {

    private final ConfigEditor<String> editor;
    private final ConfigEditor<List<Component>> argEditor;

    private String value = "";
    private AbstractWidget widget;

    private List<Component> args;
    private AbstractWidget argWidget;

    private Consumer<MutableComponent> inputChanged;

    public TranslationComponentType() {
        this.editor = ConfigEditor.input();
        this.argEditor = ConfigEditor.custom(List.of(), l -> new CollectionContent<>(l, ConfigEditor.custom(Component.empty(), ComponentContent::new), Function.identity(), true) {

            @Override
            public Component message() {
                return Component.translatable("libx.config.gui.component.arguments");
            }
        });
    }

    @Override
    public Component name() {
        return Component.translatable("libx.config.gui.component.type_translate");
    }

    @Override
    public MutableComponent defaultValue() {
        return Component.translatable("");
    }

    @Nullable
    @Override
    public MutableComponent init(Component component, Consumer<MutableComponent> inputChanged) {
        this.inputChanged = inputChanged;
        if (component.getContents() instanceof TranslatableContents tc) {
            this.value = tc.getKey();
            this.args = Arrays.stream(tc.getArgs()).map(TranslationComponentType::wrap).toList();
            return component.plainCopy();
        } else {
            return null;
        }
    }

    private void update() {
        if (this.inputChanged != null) {
            this.inputChanged.accept(Component.translatable(this.value, this.args.toArray()));
        }
    }

    @Override
    public void buildGui(Screen screen, ConfigScreenContent.ScreenManager manager, AtomicInteger y, Consumer<AbstractWidget> consumer) {
        int width = screen.width - 10;
        WidgetProperties<String> properties = new WidgetProperties<>((screen.width - width) / 2, y.get(), width, 20, value -> {
            this.value = value;
            this.update();
        });
        this.widget = EditorHelper.create(screen, this.editor, this.value, this.widget, properties);
        consumer.accept(this.widget);
        y.addAndGet(23);

        WidgetProperties<List<Component>> argProperties = new WidgetProperties<>(5, y.get(), 180, 20, args -> {
            this.args = args;
            this.update();
        });
        this.argWidget = EditorHelper.create(screen, this.argEditor, this.args, this.argWidget, argProperties);
        consumer.accept(this.argWidget);
        y.addAndGet(23);
    }

    private static Component wrap(Object obj) {
        if (obj instanceof Component c) {
            return c;
        } else if (obj instanceof Formattable f) {
            Formatter formatter = new Formatter();
            f.formatTo(formatter, 0, -1, -1);
            return Component.literal(formatter.toString());
        } else {
            return Component.literal(obj.toString());
        }
    }
}

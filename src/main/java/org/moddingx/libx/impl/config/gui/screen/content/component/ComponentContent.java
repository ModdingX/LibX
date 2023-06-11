package org.moddingx.libx.impl.config.gui.screen.content.component;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.ConfigScreenContent;
import org.moddingx.libx.config.gui.EditorOps;
import org.moddingx.libx.config.gui.WidgetProperties;
import org.moddingx.libx.impl.config.gui.EditorHelper;
import org.moddingx.libx.impl.config.gui.screen.content.CollectionContent;
import org.moddingx.libx.impl.config.gui.screen.content.component.type.KeybindComponentType;
import org.moddingx.libx.impl.config.gui.screen.content.component.type.TextComponentType;
import org.moddingx.libx.impl.config.gui.screen.content.component.type.TranslationComponentType;
import org.moddingx.libx.impl.config.gui.screen.widget.TextWidget;
import org.moddingx.libx.screen.ColorPicker;
import org.moddingx.libx.util.lazy.CachedValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public class ComponentContent implements ConfigScreenContent<Component> {

    // Non-null value means that component can't be edited, it is just
    // stored to use it as result for display.
    @Nullable
    private final Component nonEditable;

    private Consumer<Component> inputChanged;

    private final Map<ComponentType, MutableComponent> typeMap;
    private ComponentType currentType;
    private MutableComponent base;
    private StyleSetting bold;
    private StyleSetting italic;
    private StyleSetting underlined;
    private StyleSetting strikethrough;
    private StyleSetting obfuscated;
    private boolean hasColor;
    private TextColor color;
    private List<Component> siblings;

    private final CachedValue<Component> result;

    private final ConfigEditor<ComponentType> typeEditor;
    private final ConfigEditor<StyleSetting> boldEditor;
    private final ConfigEditor<StyleSetting> italicEditor;
    private final ConfigEditor<StyleSetting> underlinedEditor;
    private final ConfigEditor<StyleSetting> strikethroughEditor;
    private final ConfigEditor<StyleSetting> obfuscatedEditor;
    private final ConfigEditor<List<Component>> siblingEditor;

    private TextWidget previewWidget;
    private AbstractWidget typeWidget;
    private AbstractWidget boldWidget;
    private AbstractWidget italicWidget;
    private AbstractWidget underlinedWidget;
    private AbstractWidget strikethroughWidget;
    private AbstractWidget obfuscatedWidget;
    private ColorPicker colorWidget;
    private AbstractWidget siblingWidget;

    public ComponentContent(@Nonnull Component value) {
        List<ComponentType> types = List.of(
                new TextComponentType(),
                new TranslationComponentType(),
                new KeybindComponentType()
        );

        this.typeMap = new HashMap<>();
        ComponentType type = null;
        MutableComponent cmp = null;
        for (ComponentType t : types) {
            if (cmp == null) {
                cmp = t.init(value, c -> {
                    this.typeMap.put(t, c);
                    if (this.currentType == t) {
                        this.base = c;
                        this.update();
                    }
                });
                if (cmp != null) type = t;
            } else {
                t.init(value, c -> {
                    this.typeMap.put(t, c);
                    if (this.currentType == t) {
                        this.base = c;
                        this.update();
                    }
                });
            }
        }
        if (type == null) {
            this.nonEditable = value;
        } else {
            this.nonEditable = null;
            this.currentType = type;
            this.typeMap.put(type, cmp);
            this.base = cmp;

            // Must use value, not base as base loses all style information
            Style style = value.getStyle();
            this.bold = StyleSetting.get(style.bold);
            this.italic = StyleSetting.get(style.italic);
            this.underlined = StyleSetting.get(style.underlined);
            this.strikethrough = StyleSetting.get(style.strikethrough);
            this.obfuscated = StyleSetting.get(style.obfuscated);
            this.hasColor = style.getColor() != null;
            this.color = style.getColor() == null ? TextColor.fromRgb(0xFFFFFF) : style.getColor();

            this.siblings = value.getSiblings();
        }

        this.typeEditor = ConfigEditor.toggle(types, ComponentType::name);
        this.boldEditor = ConfigEditor.toggle(List.of(StyleSetting.INHERIT, StyleSetting.TRUE, StyleSetting.FALSE), s -> Component.translatable("libx.config.gui.component.bold", Component.translatable("libx.config.gui.component.style_setting." + s.value)));
        this.italicEditor = ConfigEditor.toggle(List.of(StyleSetting.INHERIT, StyleSetting.TRUE, StyleSetting.FALSE), s -> Component.translatable("libx.config.gui.component.italic", Component.translatable("libx.config.gui.component.style_setting." + s.value)));
        this.underlinedEditor = ConfigEditor.toggle(List.of(StyleSetting.INHERIT, StyleSetting.TRUE, StyleSetting.FALSE), s -> Component.translatable("libx.config.gui.component.underlined", Component.translatable("libx.config.gui.component.style_setting." + s.value)));
        this.strikethroughEditor = ConfigEditor.toggle(List.of(StyleSetting.INHERIT, StyleSetting.TRUE, StyleSetting.FALSE), s -> Component.translatable("libx.config.gui.component.strikethrough", Component.translatable("libx.config.gui.component.style_setting." + s.value)));
        this.obfuscatedEditor = ConfigEditor.toggle(List.of(StyleSetting.INHERIT, StyleSetting.TRUE, StyleSetting.FALSE), s -> Component.translatable("libx.config.gui.component.obfuscated", Component.translatable("libx.config.gui.component.style_setting." + s.value)));
        this.siblingEditor = ConfigEditor.custom(List.of(), l -> new CollectionContent<>(l, ConfigEditor.custom(Component.empty(), ComponentContent::new), Function.identity(), true) {

            @Override
            public Component message() {
                return Component.translatable("libx.config.gui.component.siblings");
            }
        });

        this.result = new CachedValue<>(() -> {
            if (this.nonEditable != null) return this.nonEditable;
            MutableComponent tc = this.base.copy();
            Style style = Style.EMPTY
                    .withBold(this.bold.value)
                    .withItalic(this.italic.value)
                    .withUnderlined(this.underlined.value)
                    .withStrikethrough(this.strikethrough.value)
                    .withObfuscated(this.obfuscated.value);
            if (this.hasColor) {
                style = style.withColor(this.color);
            }
            tc.setStyle(style);
            this.siblings.forEach(tc::append);
            this.inputChanged.accept(tc);
            return tc;
        });
    }

    @Override
    public Component title() {
        return Component.translatable("libx.config.gui.component.title");
    }

    @Override
    public Component message() {
        return this.result.get();
    }

    @Override
    public boolean searchable() {
        return false;
    }

    @Override
    public void init(Consumer<Component> inputChanged) {
        this.inputChanged = inputChanged;
    }

    private void update() {
        this.result.invalidate();
        if (this.previewWidget != null) {
            this.previewWidget.setMessage(this.result.get());
        }
        if (this.inputChanged != null) {
            this.inputChanged.accept(this.result.get());
        }
    }

    @Override
    public void buildGui(Screen screen, ScreenManager manager, String search, Consumer<AbstractWidget> consumer) {
        int y = 0;

        this.previewWidget = new PreviewWidget(20, y, manager.contentWidth() - 40, 36, this.result.get());
        consumer.accept(this.previewWidget);
        y += 44;

        if (this.nonEditable != null) {
            consumer.accept(new TextWidget(20, y, manager.contentWidth() - 40, 18, Component.translatable("libx.config.gui.component.no_edit"), List.of()));
            return;
        }

        WidgetProperties<ComponentType> typeProperties = new WidgetProperties<>(5, y, 180, 20, type -> {
            this.currentType = type;
            this.base = this.typeMap.getOrDefault(type, type.defaultValue());
            this.update();
            manager.rebuild();
        });
        this.typeWidget = EditorHelper.create(screen, this.typeEditor, this.currentType, this.typeWidget, typeProperties);
        consumer.accept(this.typeWidget);
        y += 27;

        AtomicInteger atomicY = new AtomicInteger(y);
        this.currentType.buildGui(screen, manager, atomicY, consumer);
        y = atomicY.get();

        y += 8;

        int width = Math.min(180, (manager.contentWidth() - (2 * 5)) / 3);
        WidgetProperties<StyleSetting> boldProperties = new WidgetProperties<>(5, y, width, 20, bold -> {
            this.bold = bold;
            this.update();
        });
        this.boldWidget = EditorHelper.create(screen, this.boldEditor, this.bold, this.boldWidget, boldProperties);
        consumer.accept(this.boldWidget);

        WidgetProperties<StyleSetting> italicProperties = new WidgetProperties<>(10 + width, y, width, 20, italic -> {
            this.italic = italic;
            this.update();
        });
        this.italicWidget = EditorHelper.create(screen, this.italicEditor, this.italic, this.italicWidget, italicProperties);
        consumer.accept(this.italicWidget);

        WidgetProperties<StyleSetting> underlinedProperties = new WidgetProperties<>(15 + (2 * width), y, width, 20, underlined -> {
            this.underlined = underlined;
            this.update();
        });
        this.underlinedWidget = EditorHelper.create(screen, this.underlinedEditor, this.underlined, this.underlinedWidget, underlinedProperties);
        consumer.accept(this.underlinedWidget);

        y += 23;

        WidgetProperties<StyleSetting> strikethroughProperties = new WidgetProperties<>(5, y, width, 20, strikethrough -> {
            this.strikethrough = strikethrough;
            this.update();
        });
        this.strikethroughWidget = EditorHelper.create(screen, this.strikethroughEditor, this.strikethrough, this.strikethroughWidget, strikethroughProperties);
        consumer.accept(this.strikethroughWidget);

        WidgetProperties<StyleSetting> obfuscatedProperties = new WidgetProperties<>(10 + width, y, width, 20, obfuscated -> {
            this.obfuscated = obfuscated;
            this.update();
        });
        this.obfuscatedWidget = EditorHelper.create(screen, this.obfuscatedEditor, this.obfuscated, this.obfuscatedWidget, obfuscatedProperties);
        consumer.accept(this.obfuscatedWidget);

        y += 25;

        Checkbox hasColorWidget = new Checkbox(14, y + ((ColorPicker.HEIGHT - 20) / 2), 20, 20, Component.empty(), this.hasColor, false) {
            @Override
            public void onPress() {
                super.onPress();
                ComponentContent.this.hasColor = this.selected();
                EditorOps.wrap(ComponentContent.this.colorWidget).enabled(ComponentContent.this.hasColor);
                ComponentContent.this.update();
            }
        };
        consumer.accept(hasColorWidget);

        this.colorWidget = new ColorPicker(screen, 37, y, this.colorWidget);
        this.colorWidget.setColor(this.color);
        this.colorWidget.setResponder(color -> {
            this.color = color;
            this.update();
        });
        EditorOps.wrap(this.colorWidget).enabled(this.hasColor);
        consumer.accept(this.colorWidget);

        y += (ColorPicker.HEIGHT + 5);

        WidgetProperties<List<Component>> siblingProperties = new WidgetProperties<>(5, y, 180, 20, siblings -> {
            this.siblings = siblings;
            this.update();
        });
        this.siblingWidget = EditorHelper.create(screen, this.siblingEditor, this.siblings, this.siblingWidget, siblingProperties);
        consumer.accept(this.siblingWidget);
    }

    private enum StyleSetting {
        INHERIT(null),
        TRUE(true),
        FALSE(false);

        @Nullable
        public final Boolean value;

        StyleSetting(@Nullable Boolean value) {
            this.value = value;
        }

        public static StyleSetting get(@Nullable Boolean value) {
            if (value == null) {
                return INHERIT;
            } else {
                return value ? TRUE : FALSE;
            }
        }
    }
}

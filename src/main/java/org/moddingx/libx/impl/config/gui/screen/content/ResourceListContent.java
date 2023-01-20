package org.moddingx.libx.impl.config.gui.screen.content;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.ConfigScreenContent;
import org.moddingx.libx.config.gui.InputProperties;
import org.moddingx.libx.config.gui.WidgetProperties;
import org.moddingx.libx.impl.config.gui.EditorHelper;
import org.moddingx.libx.impl.config.gui.editor.InputEditor;
import org.moddingx.libx.impl.config.gui.screen.widget.TextWidget;
import org.moddingx.libx.impl.config.mappers.advanced.ResourceListValueMapper;
import org.moddingx.libx.util.data.ResourceList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.IntStream;

public class ResourceListContent implements ConfigScreenContent<ResourceList> {

    private boolean allowList;
    private final List<ResourceList.RuleEntry> list;
    private Consumer<ResourceList> inputChanged;

    private final ConfigEditor<Boolean> typeEditor;
    private final ConfigEditor<Boolean> entryTypeEditor;
    private final ConfigEditor<Mode> modeEditor;

    private AbstractWidget typeWidget;
    private final List<EntryWidgets> entryWidgets;

    public ResourceListContent(ResourceList value) {
        this.allowList = value.isAllowList();
        this.list = new ArrayList<>(value.getRules());

        this.typeEditor = ConfigEditor.toggle(List.of(true, false), v -> {
            if (v) {
                return Component.translatable("libx.config.gui.resource_list.type", Component.translatable("libx.config.gui.resource_list.type_allow_list"));
            } else {
                return Component.translatable("libx.config.gui.resource_list.type", Component.translatable("libx.config.gui.resource_list.type_deny_list"));
            }
        });

        this.entryTypeEditor = ConfigEditor.toggle(List.of(false, true), v -> {
            if (v) {
                return Component.translatable("libx.config.gui.resource_list.entry_type", Component.translatable("libx.config.gui.resource_list.entry_type_regex"));
            } else {
                return Component.translatable("libx.config.gui.resource_list.entry_type", Component.translatable("libx.config.gui.resource_list.entry_type_simple"));
            }
        });

        this.modeEditor = ConfigEditor.toggle(List.of(Mode.DEFAULT, Mode.ALLOW, Mode.DENY), v -> switch (v) {
            case DEFAULT -> Component.translatable("libx.config.gui.resource_list.entry_mode", Component.translatable("libx.config.gui.resource_list.entry_mode_default"));
            case ALLOW -> Component.translatable("libx.config.gui.resource_list.entry_mode", Component.translatable("libx.config.gui.resource_list.entry_mode_allow"));
            case DENY -> Component.translatable("libx.config.gui.resource_list.entry_mode", Component.translatable("libx.config.gui.resource_list.entry_mode_deny"));
        });

        this.typeWidget = null;
        this.entryWidgets = new ArrayList<>(IntStream.range(0, this.list.size()).mapToObj(i -> EntryWidgets.EMPTY).toList());
    }

    @Override
    public Component title() {
        return Component.translatable("libx.config.gui.resource_list.title");
    }

    @Override
    public boolean searchable() {
        return false;
    }

    @Override
    public void init(Consumer<ResourceList> inputChanged) {
        this.inputChanged = inputChanged;
    }

    private void update() {
        if (this.inputChanged != null) {
            this.inputChanged.accept(new ResourceList(this.allowList, builder -> this.list.forEach(entry -> {
                try {
                    if (!entry.regex()) {
                        String prefix = "";
                        if (entry.allow() != null) {
                            prefix = entry.allow() ? "+" : "-";
                        }
                        builder.parse(prefix + entry.value());
                    } else if (entry.allow() != null) {
                        builder.regex(entry.allow(), entry.value());
                    } else {
                        builder.regex(entry.value());
                    }
                } catch (IllegalStateException | PatternSyntaxException e) {
                    // Probably invalid input before the regex state got changed
                    // Just ignore it for now. The user will see the entry go red
                    // anyway
                }
            })));
        }
    }

    @Override
    public void buildGui(Screen screen, ScreenManager manager, String search, Consumer<AbstractWidget> consumer) {
        int width = (2 * (75 + 3)) + 180 + (23 * 3);
        int padding = Math.max(0, screen.width - width) / 2;

        consumer.accept(new TextWidget(screen, (int) (padding * 0.7), 1, screen.width - (2 * padding) - 120, 18,
                Component.translatable("libx.config.gui.resource_list.info").withStyle(Style.EMPTY.withUnderlined(true).withColor(ChatFormatting.BLUE)), List.of()) {

            @Override
            public void onClick(double mouseX, double mouseY) {
                try {
                    Util.getPlatform().openUrl(ResourceListValueMapper.INFO_URL);
                } catch (Exception e) {
                    //
                }
            }
        });

        WidgetProperties<Boolean> typeProperties = new WidgetProperties<>(screen.width - padding - 120, 0, 120, 20, allowList -> {
            this.allowList = allowList;
            this.update();
        });
        AbstractWidget typeWidget = EditorHelper.create(screen, this.typeEditor, this.allowList, this.typeWidget, typeProperties);
        consumer.accept(typeWidget);
        this.typeWidget = typeWidget;

        int y = 28;
        for (int i = 0; i < this.list.size(); i++) {
            this.addEntryWidgets(screen, manager, consumer, i, y, padding);
            y += 23;
        }

        Button button = Button.builder(Component.translatable("libx.config.gui.resource_list.new"), b -> {
                    ResourceListContent.this.list.add(new ResourceList.RuleEntry("", false, null));
                    ResourceListContent.this.entryWidgets.add(EntryWidgets.EMPTY);
                    ResourceListContent.this.update();
                    manager.rebuild();
                })
                .pos(padding, y)
                .size(100, 20)
                .build();
        consumer.accept(button);
    }

    private void addEntryWidgets(Screen screen, ScreenManager manager, Consumer<AbstractWidget> consumer, int idx, int y, int padding) {
        ResourceList.RuleEntry current = this.list.get(idx);
        EntryWidgets widgets = this.entryWidgets.get(idx);

        InputProperties<String> input = new InputProperties<>() {

            @Override
            public String defaultValue() {
                return "";
            }

            @Override
            public String valueOf(String str) {
                return str;
            }

            @Override
            public boolean isValid(String str) {
                if (ResourceListContent.this.list.get(idx).regex()) {
                    try {
                        Pattern.compile(str);
                        return true;
                    } catch (PatternSyntaxException e) {
                        return false;
                    }
                } else {
                    return !str.contains("**") && ResourceLocation.tryParse(str.replace("*", "")) != null;
                }
            }
        };
        WidgetProperties<String> properties = new WidgetProperties<>(padding + 156, y, 180, 20, str -> {
            ResourceList.RuleEntry old = this.list.get(idx);
            this.list.set(idx, new ResourceList.RuleEntry(str, old.regex(), old.allow()));
            this.update();
        });
        AbstractWidget widget = EditorHelper.create(screen, ConfigEditor.input(input), current.value(), widgets.input(), properties);
        consumer.accept(widget);

        WidgetProperties<Boolean> typeProperties = new WidgetProperties<>(padding, y, 75, 20, type -> {
            ResourceList.RuleEntry old = this.list.get(idx);
            this.list.set(idx, new ResourceList.RuleEntry(old.value(), type, old.allow()));
            // After changing the regex value in the list, a previously invalid value may have become valid
            // but was not delivered due to the input properties stating it as invalid.
            // We need to take care of that
            if (widget instanceof InputEditor.InputWidget<?>) {
                //noinspection unchecked
                ((InputEditor.InputWidget<String>) widget).getValidInput().ifPresent(str -> this.list.set(idx, new ResourceList.RuleEntry(str, type, old.allow())));

            }
            this.update();
        });
        AbstractWidget typeWidget = EditorHelper.create(screen, this.entryTypeEditor, current.regex(), widgets.type(), typeProperties);
        consumer.accept(typeWidget);

        WidgetProperties<Mode> modeProperties = new WidgetProperties<>(padding + 78, y, 75, 20, mode -> {
            ResourceList.RuleEntry old = this.list.get(idx);
            this.list.set(idx, new ResourceList.RuleEntry(old.value(), old.regex(), mode.mode));
            this.update();
        });
        AbstractWidget modeWidget = EditorHelper.create(screen, this.modeEditor, Mode.get(current.allow()), widgets.mode(), modeProperties);
        consumer.accept(modeWidget);

        this.entryWidgets.set(idx, new EntryWidgets(typeWidget, modeWidget, widget));

        CollectionContent.addControlButton(consumer, padding + 339, y, Component.literal("⬆"), idx > 0, () -> {
            CollectionContent.move(this.list, idx, idx - 1);
            CollectionContent.move(this.entryWidgets, idx, idx - 1);
            this.update();
            manager.rebuild();
        });

        CollectionContent.addControlButton(consumer, padding + 362, y, Component.literal("⬇"), idx < this.list.size() - 1, () -> {
            CollectionContent.move(this.list, idx, idx + 1);
            CollectionContent.move(this.entryWidgets, idx, idx + 1);
            this.update();
            manager.rebuild();
        });

        CollectionContent.addControlButton(consumer, padding + 385, y, Component.literal("✖").withStyle(ChatFormatting.RED), true, () -> {
            this.list.remove(idx);
            this.entryWidgets.remove(idx);
            this.update();
            manager.rebuild();
        });
    }

    private enum Mode {
        DEFAULT(null),
        ALLOW(true),
        DENY(false);

        @Nullable
        public final Boolean mode;

        Mode(@Nullable Boolean mode) {
            this.mode = mode;
        }

        public static Mode get(@Nullable Boolean mode) {
            if (mode == null) {
                return DEFAULT;
            } else {
                return mode ? ALLOW : DENY;
            }
        }
    }

    private record EntryWidgets(@Nullable AbstractWidget type, @Nullable AbstractWidget mode, @Nullable AbstractWidget input) {

        public static final EntryWidgets EMPTY = new EntryWidgets(null, null, null);
    }
}

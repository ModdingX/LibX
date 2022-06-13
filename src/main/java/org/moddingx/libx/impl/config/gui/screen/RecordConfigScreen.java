package org.moddingx.libx.impl.config.gui.screen;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.moddingx.libx.config.ValidatorInfo;
import org.moddingx.libx.config.gui.WidgetProperties;
import org.moddingx.libx.impl.config.gui.EditorHelper;
import org.moddingx.libx.impl.config.wrapper.TypesafeMapper;

import javax.annotation.Nullable;
import java.lang.reflect.RecordComponent;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RecordConfigScreen extends ConfigScreen<RecordConfigScreen.Entry> {
    
    public RecordConfigScreen(ConfigScreenManager manager, Component title, List<Entry> entries) {
        super(manager, title, entries, RecordConfigScreen::createEntry, RecordConfigScreen::search);

    }
    
    private static BuiltEntry createEntry(Entry elem, ConfigScreen<Entry> screen, @Nullable AbstractWidget oldWidget, int x, int y, int width, int height) {
        WidgetProperties<Object> properties = new WidgetProperties<>(x, y, width, height, elem.inputChanged());
        return new BuiltEntry(
                Component.literal(elem.component().getName()),
                ImmutableList.of(),
                EditorHelper.create(screen, elem.mapper().createEditor(ValidatorInfo.empty()), elem.value().get(), oldWidget, properties)
        );
    }

    private static boolean search(Entry elem, ConfigScreen<Entry> screen, String query) {
        return query.isBlank() || elem.component().getName().toLowerCase().contains(query.toLowerCase());
    }
    
    public record Entry(RecordComponent component, TypesafeMapper mapper, Supplier<Object> value, Consumer<Object> inputChanged) {}
}

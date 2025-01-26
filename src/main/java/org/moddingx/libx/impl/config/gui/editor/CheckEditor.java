package org.moddingx.libx.impl.config.gui.editor;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.WidgetProperties;

public class CheckEditor implements ConfigEditor<Boolean> {

    public static final CheckEditor INSTANCE = new CheckEditor();
    
    private CheckEditor() {
        
    }
    
    @Override
    public Boolean defaultValue() {
        return false;
    }

    @Override
    public AbstractWidget createWidget(Screen screen, Boolean initialValue, WidgetProperties<Boolean> properties) {
        int paddingX = Math.max(0, properties.width() - 20) / 2;
        int paddingY = Math.max(0, properties.height() - 20) / 2;
        return Checkbox.builder(Component.empty(), screen.getMinecraft().font)
                .pos(paddingX + properties.x(), paddingY + properties.y())
                .maxWidth(properties.width())
                .selected(initialValue)
                .onValueChange((c, sel) -> properties.inputChanged().accept(sel))
                .build();
    }

    @Override
    public AbstractWidget updateWidget(Screen screen, AbstractWidget oldWidget, WidgetProperties<Boolean> properties) {
        if (oldWidget instanceof Checkbox old) {
            int paddingX = Math.max(0, properties.width() - 20) / 2;
            int paddingY = Math.max(0, properties.height() - 20) / 2;
            return Checkbox.builder(Component.empty(), screen.getMinecraft().font)
                    .pos(paddingX + properties.x(), paddingY + properties.y())
                    .maxWidth(properties.width())
                    .selected(old.selected())
                    .onValueChange((c, sel) -> properties.inputChanged().accept(sel))
                    .build();
        } else {
            return this.createWidget(screen, this.defaultValue(), properties);
        }
    }
}

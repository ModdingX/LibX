package org.moddingx.libx.impl.config.gui.editor;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
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
        int padding = Math.max(0, properties.width() - 20) / 2;
        return new Checkbox(padding + properties.x(), properties.y(), 20, properties.height(), new TextComponent(""), initialValue, false) {
            @Override
            public void onPress() {
                super.onPress();
                properties.inputChanged().accept(this.selected());
            }
        };
    }

    @Override
    public AbstractWidget updateWidget(Screen screen, AbstractWidget oldWidget, WidgetProperties<Boolean> properties) {
        if (oldWidget instanceof Checkbox old) {
            int padding = Math.max(0, properties.width() - 20) / 2;
            return new Checkbox(padding + properties.x(), properties.y(), 20, properties.height(), new TextComponent(""), old.selected(), false) {
                @Override
                public void onPress() {
                    super.onPress();
                    properties.inputChanged().accept(this.selected());
                }
            };
        } else {
            return this.createWidget(screen, this.defaultValue(), properties);
        }
    }
}

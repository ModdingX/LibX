package io.github.noeppi_noeppi.libx.config.gui;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface EditorOps {
    
    default void enabled(boolean enabled) {
        
    }
    
    default List<Component> tooltip() {
        return List.of();
    }
    
    static EditorOps wrap(Widget widget) {
        if (widget instanceof EditorOps ops) {
            return ops;
        } else if (widget instanceof AbstractWidget base) {
            return new EditorOps() {
                @Override
                public void enabled(boolean enabled) {
                    base.active = enabled;
                }
            };
        } else {
            return new EditorOps() {};
        }
    }
}

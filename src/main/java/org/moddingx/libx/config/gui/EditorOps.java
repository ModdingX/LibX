package org.moddingx.libx.config.gui;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.Optional;

/**
 * This interface can be implemented by widgets, so other {@link ConfigEditor config editors} can control them.
 * For example, an editor for {@link Optional optionals} can disable a widget if the user unsets the checkbox, so
 * no value should be available.
 */
public interface EditorOps {

    /**
     * Marks a widget as enabled or disabled.
     */
    default void enabled(boolean enabled) {
        
    }

    /**
     * Wraps a {@link GuiEventListener} into matching editor ops.
     */
    static EditorOps wrap(GuiEventListener widget) {
        return switch (widget) {
            case EditorOps ops -> ops;
            case EditBox base -> new EditorOps() {
                @Override
                public void enabled(boolean enabled) {
                    base.setEditable(enabled);
                    base.active = enabled;
                }
            };
            case AbstractWidget base -> new EditorOps() {
                @Override
                public void enabled(boolean enabled) {
                    base.active = enabled;
                }
            };
            case null, default -> new EditorOps() {};
        };
    }
}

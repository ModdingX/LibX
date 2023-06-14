package org.moddingx.libx.screen.text.entry;

import net.minecraft.client.gui.components.AbstractWidget;
import org.moddingx.libx.screen.text.TextScreen;

/**
 * An {@link AbstractWidget} that has been aligned for display on a {@link TextScreen}.
 * 
 * @param widget The widget to display. The widgets {@link AbstractWidget#getX() x} and {@link AbstractWidget#getX() y}
 *               coordinates are ignored and will be automatically set when the widget is placed on the screen.
 * @param left The horizontal padding to the left edge of the screen.
 * @param top The vertical padding to the bottom of the previous component.
 */
public record AlignedWidget(AbstractWidget widget, int left, int top) implements TextScreenEntry.Direct {

}

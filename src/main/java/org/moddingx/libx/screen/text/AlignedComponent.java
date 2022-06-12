package org.moddingx.libx.screen.text;

import net.minecraft.network.chat.Component;

/**
 * A {@link Component} that has been aligned for display on a {@link TextScreen}.
 * 
 * @param text The component to display.
 * @param left The horizontal padding to the left edge of the screen.
 * @param top The vertical padding to the bottom of the previous component.
 * @param wrap Whether this component should be wrapped.
 * @param color The default display colour for the component. This can be altered by the style.
 * @param shadow Whether the component should be drawn with a shadow.
 */
public record AlignedComponent(Component text, int left, int top, boolean wrap, int color, boolean shadow) {
    
    public AlignedComponent(Component text, int left, int top, boolean wrap) {
        this(text, left, top, wrap, 0x000000, false);
    }
    
    public AlignedComponent(Component text, int left, int top) {
        this(text, left, top, true);
    }
}

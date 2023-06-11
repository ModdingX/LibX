package org.moddingx.libx.screen.text;

import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

/**
 * A {@link Component} that has been aligned for display on a {@link TextScreen}.
 * 
 * @param text The component to display.
 * @param left The horizontal padding to the left edge of the screen.
 * @param top The vertical padding to the bottom of the previous component.
 * @param wrapping A {@link TextWrapping} describing the wrapping properties of this component or {@code null} if
 *                 this component should not be wrapped.
 * @param color The default display colour for the component. This can be altered by the style.
 * @param shadow Whether the component should be drawn with a shadow.
 */
public record AlignedComponent(Component text, int left, int top, @Nullable TextWrapping wrapping, int color, boolean shadow) implements TextScreenEntry.Direct {
    
    public AlignedComponent(Component text, int left, int top, TextWrapping wrap) {
        this(text, left, top, wrap, 0x000000, false);
        
    }
    
    public AlignedComponent(Component text, int left, int top, boolean wrap) {
        this(text, left, top, wrap ? new TextWrapping(0) : null);
    }
    
    public AlignedComponent(Component text, int left, int top) {
        this(text, left, top, true);
    }
    
    /**
     * Specifies text-wrapping properties.
     * @param minWidth The minimum width the component can shrink to when wrapped. Useful inside
     *                 {@link FlowBox flow boxes}.
     */
    public record TextWrapping(int minWidth) {}
}

package org.moddingx.libx.screen.text;

/**
 * An object that has been aligned for display on a {@link TextScreen}.
 */
public sealed interface TextScreenEntry permits TextScreenEntry.Direct, FlowBox {

    int left();
    int top();
    
    /**
     * An object that has been aligned for display on a {@link TextScreen} or a {@link FlowBox}.
     */
    sealed interface Direct extends TextScreenEntry permits AlignedComponent, AlignedWidget {
        
    }
}

package org.moddingx.libx.screen.text;

import java.util.List;

/**
 * A flow box can align elements horizontally inside a {@link TextScreen} that align items vertically by default.
 * Elements will be split into multiple rows, where each row is filled with as many elements as possible before
 * they overflow. Note that {@link AlignedComponent#wrapping() wrapped components} can shrink in width. It is
 * recommended to set a {@link AlignedComponent.TextWrapping#minWidth() minimum width} when using wrapped components
 * inside a flow box.
 * 
 * Inside the flow box, {@link TextScreenEntry#left() left padding} will be added to each element and describes the
 * distance to the previous element in the row. The first element in a row won't get left padding, to pad the start of
 * a row, pad the entire flow box.
 * 
 * All elements in a row will get the same {@link TextScreenEntry#top() top padding} which is the maximum of the
 * individual top paddings inside that row.
 * 
 * Flow boxes can also define {@link #right() right padding} which is useful when the flow box uses
 * {@link HorizontalAlignment#RIGHT} alignment.
 * 
 * @param horizontalAlignment How the elements should be aligned horizontally.
 * @param verticalAlignment How the elements should be aligned vertically.
 * @param elements The elements inside this box.
 * @param left The horizontal padding to the left edge of the screen.
 * @param top The vertical padding to the bottom of the previous component.
 */
public record FlowBox(HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment, List<TextScreenEntry.Direct> elements, int left, int right, int top) implements TextScreenEntry {

    public FlowBox(List<TextScreenEntry.Direct> elements, int horizontalPadding, int top) {
        this(elements, horizontalPadding, horizontalPadding, top);
    }

    public FlowBox(HorizontalAlignment alignment, List<TextScreenEntry.Direct> elements, int horizontalPadding, int top) {
        this(alignment, elements, horizontalPadding, horizontalPadding, top);
    }

    public FlowBox(HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment, List<TextScreenEntry.Direct> elements, int horizontalPadding, int top) {
        this(horizontalAlignment, verticalAlignment, elements, horizontalPadding, horizontalPadding, top);
    }

    public FlowBox(List<TextScreenEntry.Direct> elements, int left, int right, int top) {
        this(HorizontalAlignment.LEFT, elements, left, right, top);
    }
    
    public FlowBox(HorizontalAlignment alignment, List<TextScreenEntry.Direct> elements, int left, int right, int top) {
        this(alignment, VerticalAlignment.CENTER, elements, left, right, top);
    }

    /**
     * Specifies the horizontal alignment of elements inside a {@link FlowBox}.
     */
    public enum HorizontalAlignment {
        
        /**
         * Elements in a row are aligned flush left, empty space on the right.
         */
        LEFT,
        
        /**
         * Elements in a row are aligned flush right, empty space on the left.
         */
        RIGHT,
        
        /**
         * Elements in a row are aligned in the center, empty space is evently distributed left and right.
         */
        CENTER
    }

    /**
     * Specifies the horizontal alignment of elements inside a {@link FlowBox}.
     */
    public enum VerticalAlignment {

        /**
         * Elements in a row share a common top line.
         */
        TOP,
        
        /**
         * Elements in a row share a common center line.
         */
        CENTER,
        
        /**
         * Elements in a row share a common bottom line.
         */
        BOTTOM
    }
}

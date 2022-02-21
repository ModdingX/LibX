package io.github.noeppi_noeppi.libx.screen.text;

import io.github.noeppi_noeppi.libx.impl.screen.text.SimpleLayout;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * A layout that aligns components on a {@link TextScreen}.
 */
public interface ComponentLayout {

    /**
     * Gets the screen title if available.
     */
    @Nullable
    default Component title() {
        return null;
    }

    /**
     * Aligns some components for the screen.
     * 
     * @param font The font that is used to display the content.
     * @param width The available screen width
     */
    List<AlignedComponent> alignComponents(Font font, int width);

    /**
     * Creates a simple {@link ComponentLayout} that aligns the given components below each other.
     */
    static ComponentLayout simple(Component... content) {
        return simple(null, Arrays.stream(content).toList());
    }

    /**
     * Creates a simple {@link ComponentLayout} that aligns the given components below each other.
     * The title is displayed centered above the other text.
     */
    static ComponentLayout simple(@Nullable Component title, List<Component> content) {
        return new SimpleLayout(title, content);
    }
}

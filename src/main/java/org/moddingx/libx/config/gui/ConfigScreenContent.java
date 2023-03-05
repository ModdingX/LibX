package org.moddingx.libx.config.gui;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * Interface that defines content for custom config editor screens.
 */
public interface ConfigScreenContent<T> {

    /**
     * Gets the title of the screen.
     */
    Component title();

    /**
     * Gets the message for the button that opens the screen.
     */
    default Component message() {
        return Component.translatable("libx.config.gui.edit");
    }

    /**
     * Whether this screen is searchable
     */
    boolean searchable();

    /**
     * Initialises the screen.
     * 
     * @param inputChanged A consumer that should be called with the new value whenever the input changes.
     */
    void init(Consumer<T> inputChanged);

    /**
     * Builds the screen content.
     * 
     * @param screen The screen that is used. This can be used to get the available width. Height is unlimited
     *               because the screen is scrollable. Also, this <b>must</b> be used if the screen wants
     *               to draw tooltips, or they won't work correctly.
     * @param manager A {@link ScreenManager} that provides access to the internal screen history and can be
     *                used to rebuild the screen.
     * @param search The current search term or an empty {@link String} if there is none.
     * @param consumer A consumer that will consume all widgets that should be displayed.
     */
    void buildGui(Screen screen, ScreenManager manager, String search, Consumer<AbstractWidget> consumer);

    /**
     * Provides access to some screen operations. The methods in here <b>must</b> be used instead of the regular
     * methods or the config won't get saved correctly.
     */
    interface ScreenManager {

        /**
         * The maximum width, the content can occupy. This should be used instead of {@link Screen#width}.
         */
        int contentWidth();
        
        /**
         * Rebuilds the current screen content. This causes {@link #buildGui(Screen, ScreenManager, String, Consumer)}
         * to be called again.
         */
        void rebuild();

        /**
         * Opens a new sub-screen
         * 
         * @param content The content to display on the sub screen.
         * @param inputChanged A consumer that is called with a new value whenever the input changes.
         */
        <T> void open(ConfigScreenContent<T> content, Consumer<T> inputChanged);

        /**
         * Closes the current screen.
         */
        void close();
    }
}

package io.github.noeppi_noeppi.libx.config.gui;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.function.Consumer;

// Must keep state between builds
public interface ConfigScreenContent<T> {
    
    Component title();
    
    default Component message() {
        return new TranslatableComponent("libx.config.gui.edit");
    }
    
    boolean searchable();
    
    void init(Consumer<T> inputChanged);
    void buildGui(Screen screen, ScreenManager manager, String search, Consumer<AbstractWidget> consumer);
    
    interface ScreenManager {
        
        void rebuild();
        
        <T> void open(ConfigScreenContent<T> content, Consumer<T> inputChanged);
        void close();
    }
}

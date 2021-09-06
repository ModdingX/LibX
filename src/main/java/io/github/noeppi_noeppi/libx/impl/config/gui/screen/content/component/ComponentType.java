package io.github.noeppi_noeppi.libx.impl.config.gui.screen.content.component;

import io.github.noeppi_noeppi.libx.config.gui.ConfigScreenContent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public interface ComponentType<T extends MutableComponent> {
    
    Component name();
    T defaultValue();
    
    // Non-null value = component is of matching type
    // State has been initialised
    // Result component must have all style and siblings removed
    @Nullable
    MutableComponent init(Component component, Consumer<T> inputChanged);
    void buildGui(Screen screen, ConfigScreenContent.ScreenManager manager, AtomicInteger y, Consumer<AbstractWidget> consumer);
    
}

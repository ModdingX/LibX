package org.moddingx.libx.impl.config.gui.screen.content.component;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.moddingx.libx.config.gui.ConfigScreenContent;

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

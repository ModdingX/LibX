package io.github.noeppi_noeppi.libx.impl.config.gui.screen;

import io.github.noeppi_noeppi.libx.impl.config.ConfigImpl;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;
import java.util.function.Function;

public class ConfigSelectScreen extends Screen {

    private final Function<ConfigImpl, Screen> factory;
    private final List<ConfigImpl> configs;
    
    public ConfigSelectScreen(Function<ConfigImpl, Screen> factory, List<ConfigImpl> configs) {
        super(new TranslatableComponent("libx.config.gui.selection.title"));
        this.factory = factory;
        this.configs = configs;
    }
}

package io.github.noeppi_noeppi.libx.impl.config.gui.screen;

import io.github.noeppi_noeppi.libx.config.gui.ConfigScreenContent;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.function.Consumer;

public class CustomConfigScreen<T> extends ConfigBaseScreen {

    private final ConfigScreenContent<T> content;
    private final ConfigScreenContent.ScreenManager contentManager;
    
    public CustomConfigScreen(ConfigScreenManager manager, ConfigScreenContent<T> content) {
        super(content.title(), manager, content.searchable());
        this.content = content;
        this.contentManager = new ConfigScreenContent.ScreenManager() {

            @Override
            public void rebuild() {
                CustomConfigScreen.this.rebuild();
            }

            @Override
            public <X> void open(ConfigScreenContent<X> content, Consumer<X> inputChanged) {
                content.init(inputChanged);
                manager.open(new CustomConfigScreen<>(manager, content));
            }

            @Override
            public void close() {
                manager.close();
            }
        };
    }

    @Override
    protected void buildGui(Consumer<AbstractWidget> consumer) {
        this.content.buildGui(this, this.contentManager, this.searchTerm(), consumer);
    }
}

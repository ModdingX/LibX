package org.moddingx.libx.impl.config.gui.screen;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.moddingx.libx.impl.config.ConfigImpl;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConfigSelectScreen extends ConfigBaseScreen {

    private final Function<ConfigImpl, Screen> factory;
    private final List<ConfigImpl> configs;
    private final Screen root;
    
    public ConfigSelectScreen(Function<ConfigImpl, Screen> factory, List<ConfigImpl> configs, Screen root) {
        super(Component.translatable("libx.config.gui.selection.title"), null, false);
        this.factory = factory;
        this.configs = configs;
        this.root = root;
    }

    @Override
    protected void buildGui(Consumer<AbstractWidget> consumer) {
        int y = 5;
        int buttonWidth = Math.min(200, this.contentWidth() - 10);
        for (ConfigImpl config : this.configs) {
            Button button = Button.builder(Component.literal(config.id.getPath()), b -> this.mc.setScreen(this.factory.apply(config)))
                    .pos((this.width - buttonWidth) / 2, y)
                    .size(buttonWidth, 20)
                    .build();
            consumer.accept(button);
            y += 25;
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        // The manager is not available in the select screen, so we need to handle escape ourselves
        if (event.key() == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc()) {
            this.mc.setScreen(this.root);
            return true;
        } else {
            return super.keyPressed(event);
        }
    }
}

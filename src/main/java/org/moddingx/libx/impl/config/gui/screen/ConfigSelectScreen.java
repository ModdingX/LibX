package org.moddingx.libx.impl.config.gui.screen;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.moddingx.libx.impl.config.ConfigImpl;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class ConfigSelectScreen extends ConfigBaseScreen {

    private final BiFunction<ConfigImpl, Screen, Screen> factory;
    private final List<ConfigImpl> configs;
    private final Screen root;
    
    public ConfigSelectScreen(BiFunction<ConfigImpl, Screen, Screen> factory, List<ConfigImpl> configs, Screen root) {
        super(Component.translatable("libx.config.gui.selection.title"), null, false);
        this.factory = factory;
        this.configs = configs;
        this.root = root;
    }

    @Override
    protected void buildGui(Consumer<AbstractWidget> consumer) {
        Button back = Button.builder(Component.literal("\u2190 ").append(Component.translatable("libx.config.gui.back")), button -> this.mc.setScreen(this.root))
                .pos(5, 5)
                .size(52, 20)
                .build();
        this.addRenderableWidget(back);

        int y = 5;
        int buttonWidth = Math.min(200, this.contentWidth() - 10);
        for (ConfigImpl config : this.configs) {
            Button button = Button.builder(Component.literal(config.id.getPath()), b -> {
                        Screen configSelectScreen = this.factory.apply(config, this);
                        this.mc.setScreen(configSelectScreen);
                    })
                    .pos((this.contentWidth() - buttonWidth) / 2, y)
                    .size(buttonWidth, 20)
                    .build();
            consumer.accept(button);
            y += 25;
        }
    }

    @Override
    public boolean keyPressed(int key, int i1, int i2) {
        // The manager is not available in the select screen, so we need to handle escape ourselves
        if (key == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc()) {
            this.mc.setScreen(this.root);
            return true;
        } else {
            return super.keyPressed(key, i1, i2);
        }
    }
}

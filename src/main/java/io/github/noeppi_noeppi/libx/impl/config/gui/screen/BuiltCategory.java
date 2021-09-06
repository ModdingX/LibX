package io.github.noeppi_noeppi.libx.impl.config.gui.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.List;

public record BuiltCategory(String id, Component title, List<Component> description) {
    
    public static final BuiltCategory EMPTY = new BuiltCategory("", new TextComponent(""), List.of());
}

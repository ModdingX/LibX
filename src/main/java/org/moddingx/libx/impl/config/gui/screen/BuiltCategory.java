package org.moddingx.libx.impl.config.gui.screen;

import net.minecraft.network.chat.Component;

import java.util.List;

public record BuiltCategory(String id, Component title, List<Component> description) {
    
    public static final BuiltCategory EMPTY = new BuiltCategory("", Component.empty(), List.of());
}

package io.github.noeppi_noeppi.libx.config.gui;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Component;

public record ConfigEditorEntry(Component name, ImmutableList<Component> description, ConfigEditor<?> editor) {}

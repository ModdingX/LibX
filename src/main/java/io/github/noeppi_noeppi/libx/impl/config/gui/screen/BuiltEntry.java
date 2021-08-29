package io.github.noeppi_noeppi.libx.impl.config.gui.screen;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

public record BuiltEntry(Component title, ImmutableList<? extends Component> description, AbstractWidget widget) {}

package io.github.noeppi_noeppi.libx.config.gui;

import java.util.function.Consumer;

public record WidgetProperties<T>(int x, int y, int width, int height, Consumer<T> inputChanged) {}

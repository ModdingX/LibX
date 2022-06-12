package org.moddingx.libx.config.gui;

import java.util.function.Consumer;

/**
 * Properties for a widget created by a {@link ConfigEditor}.
 * @param x The x coordinate to be used for the widget.
 * @param y The y coordinate to be used for the widget.
 * @param width The width available for the widget.
 * @param height The height available for the widget.
 * @param inputChanged A consumer that should be called with the new value whenever the input changes.
 */
public record WidgetProperties<T>(int x, int y, int width, int height, Consumer<T> inputChanged) {}

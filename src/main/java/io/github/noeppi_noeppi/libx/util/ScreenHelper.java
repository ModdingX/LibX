package io.github.noeppi_noeppi.libx.util;

/**
 * Some utilities for working with screens.
 */
public class ScreenHelper {

    /**
     * Checks if a position is within a certain area.
     *
     * @param x      Start x position
     * @param y      Start y position
     * @param width  Width of area
     * @param height Height of area
     * @param ox     Offset x to check
     * @param oy     Offset y to check
     * @return Whether the offset positions are in the given area.
     */
    public static boolean inBounds(int x, int y, int width, int height, double ox, double oy) {
        return ox >= x && ox <= x + width && oy >= y && oy <= y + height;
    }
}

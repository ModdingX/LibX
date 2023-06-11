package org.moddingx.libx.datagen.provider.texture;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Helper methods to transform {@link BufferedImage images}.
 */
public class ImageTransforms {

    /**
     * Rotates the give image by the given rotation.
     */
    public static BufferedImage rotate(BufferedImage src, Rotation rotation) {
        if (rotation == Rotation.NONE) return src;
        int width = rotation == Rotation.CLOCKWISE_180 ? src.getWidth() : src.getHeight();
        int height = rotation == Rotation.CLOCKWISE_180 ? src.getHeight() : src.getWidth();
        int degrees;
        int tx;
        int ty;
        switch (rotation) {
            case CLOCKWISE_90 -> {
                tx = 0;
                ty = -src.getHeight();
                degrees = 90;
            }
            case CLOCKWISE_180 -> {
                degrees = 180;
                tx = -src.getWidth();
                ty = -src.getHeight();
            }
            case COUNTERCLOCKWISE_90 -> {
                tx = -src.getWidth();
                ty = 0;
                degrees = 270;
            }
            default -> throw new Error();
        }
        
        BufferedImage dest = new BufferedImage(width, height, src.getType());
        
        Graphics2D g = dest.createGraphics();
        g.rotate(Math.toRadians(degrees));
        g.translate(tx, ty);
        g.drawRenderedImage(src, null);
        
        return dest;
    }

    /**
     * Flips the give image by the given axis. Flipping by {@link Direction.Axis#Z} does nothing.
     */
    public static BufferedImage flip(BufferedImage src, Direction.Axis axis) {
        if (axis == Direction.Axis.Z) return src;

        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        
        Graphics2D g = dest.createGraphics();
        if (axis == Direction.Axis.X) {
            g.scale(-1, 1);
            g.translate(-src.getWidth(), 0);
        } else {
            g.scale(1, -1);
            g.translate(0, -src.getHeight());
        }
        g.drawRenderedImage(src, null);
        
        return dest;
    }
}

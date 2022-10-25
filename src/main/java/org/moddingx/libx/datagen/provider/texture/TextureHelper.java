package org.moddingx.libx.datagen.provider.texture;

import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.function.Function;

/**
 * Helper functions for custom {@link TextureFactory texture factories}.
 * 
 * See {@link TextureProviderBase} for the difference between <i>image</i> and <i>texture</i> ids
 * and for information on scaling.
 */
public class TextureHelper {

    /**
     * Copies a texture to an image.
     * 
     * @see #copyImage(BufferedImage, Textures, ResourceLocation, int, int, int, int, int, int)
     */
    public static void copyTexture(BufferedImage image, Textures textures, String src, int x, int y, int width, int height, int u, int v) {
        doCopyImage(image, textures, textures.texture(src), textures.textureScale(src), x, y, width, height, u, v);
    }

    /**
     * Copies an image to another image.
     * 
     * @see #copyImage(BufferedImage, Textures, ResourceLocation, int, int, int, int, int, int)
     */
    public static void copyImage(BufferedImage image, Textures textures, String src, int x, int y, int width, int height, int u, int v) {
        doCopyImage(image, textures, textures.image(src), textures.imageScale(src), x, y, width, height, u, v);
    }
    
    /**
     * Copies a texture to an image.
     * 
     * @see #copyImage(BufferedImage, Textures, ResourceLocation, int, int, int, int, int, int)
     */
    public static void copyTexture(BufferedImage image, Textures textures, ResourceLocation src, int x, int y, int width, int height, int u, int v) {
        doCopyImage(image, textures, textures.texture(src), textures.textureScale(src), x, y, width, height, u, v);
    }

    /**
     * Copies an image to another image.
     * 
     * @param image The target image.
     * @param textures A {@link Textures} object to resolve the source image and scales.
     * @param src The source image location.
     * @param x Unscaled x position on target image.
     * @param y Unscaled y position on target image.
     * @param width Unscaled width of area to copy.
     * @param height Unscaled height of area to copy.
     * @param u Unscaled x position on source image.
     * @param v Unscaled y position on source image.
     */
    public static void copyImage(BufferedImage image, Textures textures, ResourceLocation src, int x, int y, int width, int height, int u, int v) {
        doCopyImage(image, textures, textures.image(src), textures.imageScale(src), x, y, width, height, u, v);
    }

    private static void doCopyImage(BufferedImage image, Textures textures, BufferedImage src, int srcScale, int x, int y, int width, int height, int u, int v) {
        doCopyImage(image, src, x * textures.scale(), y * textures.scale(), width * textures.scale(), height * textures.scale(), u * srcScale, v * srcScale, width * srcScale, height * srcScale);
    }
    
    private static void doCopyImage(BufferedImage image, BufferedImage src, int x, int y, int width, int height, int u, int v, int uw, int vh) {
        observer().waitFor(o -> image.getGraphics().drawImage(src, x, y, x + width, y + height, u, v, u + uw, v + vh, o));
    }

    /**
     * Clears an area on an image. All pixels will be set transparent.
     *
     * @param image The image to clear.
     * @param textures A {@link Textures} object to resolve scales.
     * @param x Unscaled x position.
     * @param y Unscaled y position.
     * @param width Unscaled width of area to clear.
     * @param height Unscaled height of area to clear.
     */
    public static void clear(BufferedImage image, Textures textures, int x, int y, int width, int height) {
        // Can't clear with graphics as it does not work with alpha.
        int[] data = new int[(width * textures.scale()) * (height * textures.scale())];
        image.setRGB(x * textures.scale(), y * textures.scale(), width * textures.scale(), height * textures.scale(), data, 0, width * textures.scale());
    }
    
    private static WaitingObserver observer() {
        return new WaitingObserver("");
    }
    
    private static class WaitingObserver implements ImageObserver {

        private final Object lock = new Object();
        private final String description;
        
        private boolean success = false;
        private RuntimeException ex = null;

        private WaitingObserver(String description) {
            this.description = description;
        }

        @Override
        public boolean imageUpdate(Image img, int flags, int x, int y, int width, int height) {
            synchronized (this.lock) {
                if (!this.success && this.ex == null) {
                    if ((flags & ImageObserver.ABORT) != 0) {
                        this.ex = new IllegalStateException("Image operation aborted");
                        this.lock.notifyAll();
                        return false;
                    } else if ((flags & ImageObserver.ERROR) != 0) {
                        this.ex = new IllegalStateException("Image operation failed");
                        this.lock.notifyAll();
                        return false;
                    } else if ((flags & ImageObserver.ALLBITS) != 0 || (flags & ImageObserver.FRAMEBITS) != 0) {
                        this.success = true;
                        this.notifyAll();
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    return false;
                }
            }
        }

        public void waitFor(Function<ImageObserver, Boolean> instantSuccess) {
            if (!instantSuccess.apply(this)) {
                this.waitFor();
            }
        }
        
        public void waitFor() {
            synchronized (this.lock) {
                if (this.success) {
                    return;
                } else if (this.ex != null) {
                    throw new RuntimeException("Waiting for image " + this.description + " failed", this.ex);
                }
                while (true) {
                    try {
                        this.lock.wait();
                        if (this.success) {
                            return;
                        } else if (this.ex != null) {
                            throw new RuntimeException("Waiting for image " + this.description + " failed", this.ex);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
}

package org.moddingx.libx.datagen.provider.texture;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

/**
 * A factory to generate a texture.
 */
public interface TextureFactory {

    /**
     * Gets the unscaled size of the texture.
     */
    Dimension getSize();

    /**
     * Adds textures required to build this texture to the given builder.
     */
    void addTextures(TextureBuilder builder);
    
    /**
     * Fills the given image.
     */
    void generate(BufferedImage image, Textures textures);
}

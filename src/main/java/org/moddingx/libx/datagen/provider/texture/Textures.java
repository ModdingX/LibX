package org.moddingx.libx.datagen.provider.texture;

import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.moddingx.libx.LibX;
import org.moddingx.libx.mod.ModX;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.function.Function;

/**
 * Provides preloaded textures, a computed target texture scale and source texture scales.
 * 
 * See {@link TextureProviderBase} for the difference between <i>image</i> and <i>texture</i> ids
 * and for information on scaling.
 */
public class Textures {

    private final ModX mod;
    private final Function<ResourceLocation, BufferedImage> textureLoader;
    
    private final int scale;
    private final Map<ResourceLocation, Pair<BufferedImage, Integer>> images;

    public Textures(ModX mod, Function<ResourceLocation, BufferedImage> textureLoader, int scale, Map<ResourceLocation, Pair<BufferedImage, Integer>> images) {
        this.mod = mod;
        this.textureLoader = textureLoader;
        this.scale = scale;
        this.images = Map.copyOf(images);
    }

    /**
     * Gets the target texture scale. This indicates, how much the requested size has been scaled up.
     */
    public int scale() {
        return this.scale;
    }

    /**
     * Gets the scale for a source texture. This indicates, how much the given source texture needs to be
     * scaled up to match the target scale.
     */
    public int textureScale(String loc) {
        return this.textureScale(this.mod.resource(loc));
    }

    /**
     * Gets the scale for a source image. This indicates, how much the given source image needs to be
     * scaled up to match the target scale.
     */
    public int imageScale(String loc) {
        return this.imageScale(this.mod.resource(loc));
    }

    /**
     * Gets the scale for a source texture. This indicates, how much the given source texture needs to be
     * scaled up to match the target scale.
     */
    public int textureScale(ResourceLocation loc) {
        return this.imageScale(new ResourceLocation(loc.getNamespace(), "textures/" + loc.getPath() + ".png"));
    }

    /**
     * Gets the scale for a source image. This indicates, how much the given source image needs to be
     * scaled up to match the target scale.
     */
    public int imageScale(ResourceLocation loc) {
        if (this.images.containsKey(loc)) {
            return this.scale / this.images.get(loc).getRight();
        } else {
            LibX.logger.warn("Requesting texture scale for " + loc + " after scale was built. It should be added to the texture builder.");
            return 1;
        }
    }

    /**
     * Gets a preloaded source texture.
     */
    public BufferedImage texture(String loc) {
        return this.texture(this.mod.resource(loc));
    }

    /**
     * Gets a preloaded source image.
     */
    public BufferedImage image(String loc) {
        return this.image(this.mod.resource(loc));
    }

    /**
     * Gets a preloaded source texture.
     */
    public BufferedImage texture(ResourceLocation loc) {
        return this.image(new ResourceLocation(loc.getNamespace(), "textures/" + loc.getPath() + ".png"));
    }

    /**
     * Gets a preloaded source image.
     */
    public BufferedImage image(ResourceLocation loc) {
        if (this.images.containsKey(loc)) {
            return this.images.get(loc).getLeft();
        } else {
            LibX.logger.warn("Loading texture " + loc + " after scale was built. It should be added to the texture builder.");
            return this.textureLoader.apply(loc);
        }
    }
}

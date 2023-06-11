package org.moddingx.libx.datagen.provider.texture;

import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.moddingx.libx.mod.ModX;

import java.awt.image.BufferedImage;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * A texture builder is used to preload required textures to build another texture and compute
 * scales for the source textures and the target texture. It is used to build a {@link Textures}
 * object.
 * 
 * See {@link TextureProviderBase} for the difference between <i>image</i> and <i>texture</i> ids
 * and for information on scaling.
 */
public class TextureBuilder {

    private final ModX mod;
    private final Function<ResourceLocation, BufferedImage> textureLoader;
    
    private int scale;
    private final Map<ResourceLocation, Pair<BufferedImage, Integer>> images;
    private final Map<ResourceLocation, Pair<BufferedImage, Integer>> fakes;

    public TextureBuilder(ModX mod, Function<ResourceLocation, BufferedImage> textureLoader) {
        this.mod = mod;
        this.textureLoader = textureLoader;
        
        this.scale = 1;
        this.images = new HashMap<>();
        this.fakes = new HashMap<>();
    }

    /**
     * Adds a required texture which has the given size by default as width and height.
     */
    public TextureBuilder addTexture(String loc, int defaultSize) {
        return this.addTexture(loc, defaultSize, defaultSize);
    }

    /**
     * Adds a required image which has the given size by default as width and height.
     */
    public TextureBuilder addImage(String loc, int defaultSize) {
        return this.addImage(loc, defaultSize, defaultSize);
    }

    /**
     * Adds a required texture which has the given size by default as width and height.
     */
    public TextureBuilder addTexture(ResourceLocation loc, int defaultSize) {
        return this.addTexture(loc, defaultSize, defaultSize);
    }

    /**
     * Adds a required image which has the given size by default as width and height.
     */
    public TextureBuilder addImage(ResourceLocation loc, int defaultSize) {
        return this.addImage(loc, defaultSize, defaultSize);
    }

    /**
     * Adds a required texture which has the given width and height by default.
     */
    public TextureBuilder addTexture(String loc, int defaultWidth, int defaultHeight) {
        return this.addTexture(this.mod.resource(loc), defaultWidth, defaultHeight);
    }

    /**
     * Adds a required image which has the given width and height by default.
     */
    public TextureBuilder addImage(String loc, int defaultWidth, int defaultHeight) {
        return this.addImage(this.mod.resource(loc), defaultWidth, defaultHeight);
    }

    /**
     * Adds a required texture which has the given width and height by default.
     */
    public TextureBuilder addTexture(ResourceLocation loc, int defaultWidth, int defaultHeight) {
        return this.addImage(new ResourceLocation(loc.getNamespace(), "textures/" + loc.getPath() + ".png"), defaultWidth, defaultHeight);
    }

    /**
     * Assigns the given id to a fake image.
     *
     * @param loc A fake <i>image</i> id.
     */
    public TextureBuilder addFake(ResourceLocation loc, BufferedImage image) {
        return this.addFake(loc, image, 1);
    }

    /**
     * Assigns the given id to a fake image and scale.
     *
     * @param loc A fake <i>image</i> id.
     */
    public TextureBuilder addFake(ResourceLocation loc, BufferedImage image, int scale) {
        if (this.fakes.containsKey(loc)) throw new IllegalStateException("Duplicate fake texture: " + loc);
        this.fakes.put(loc, Pair.of(image, scale));
        return this;
    }

    /**
     * Assigns the given id to a fake image that is obtained by transforming another image.
     * This requires that the texture {@code texLoc} has already been added.
     * 
     * @param loc A fake <i>image</i> id.
     */
    public TextureBuilder addFakeTexture(ResourceLocation loc, ResourceLocation texLoc, UnaryOperator<BufferedImage> image) {
        return this.addFakeImage(loc, new ResourceLocation(texLoc.getNamespace(), "textures/" + texLoc.getPath() + ".png"), image);
    }

    /**
     * Assigns the given id to a fake image that is obtained by transforming another image.
     * This requires that the image {@code imgLoc} has already been added.
     *
     * @param loc A fake <i>image</i> id.
     */
    public TextureBuilder addFakeImage(ResourceLocation loc, ResourceLocation imgLoc, UnaryOperator<BufferedImage> image) {
        if (!this.images.containsKey(imgLoc)) throw new IllegalStateException("Can't add fake transform of non-loaded image: " + imgLoc);
        if (this.fakes.containsKey(loc)) throw new IllegalStateException("Duplicate fake texture: " + loc);
        Pair<BufferedImage, Integer> original = this.images.get(imgLoc);
        this.fakes.put(loc, Pair.of(image.apply(original.getLeft()), original.getRight()));
        return this;
    }

    /**
     * Adds a required image which has the given width and height by default.
     */
    public TextureBuilder addImage(ResourceLocation loc, int defaultWidth, int defaultHeight) {
        if (!isPowerOfTwo(defaultWidth)) throw new IllegalArgumentException("Invalid default width for texture " + loc + ": " + defaultWidth + " is not a power of two.");
        if (!isPowerOfTwo(defaultHeight)) throw new IllegalArgumentException("Invalid default height for texture " + loc + ": " + defaultHeight + " is not a power of two.");
        if (!this.images.containsKey(loc)) {
            BufferedImage image = this.textureLoader.apply(loc);
            if (!isPowerOfTwo(image.getWidth())) throw new IllegalStateException("Invalid texture width for texture " + loc + ": " + image.getWidth() + " is not a power of two.");
            if (!isPowerOfTwo(image.getHeight())) throw new IllegalStateException("Invalid texture height for texture " + loc + ": " + image.getHeight() + " is not a power of two.");
            if (image.getWidth() < defaultWidth || image.getHeight() < defaultHeight) throw new IllegalStateException("Invalid texture: " + loc + ": Image is smaller than default");
            int imageScaleByWidth = image.getWidth() / defaultWidth;
            int imageScaleByHeight = image.getHeight() / defaultHeight;
            if (imageScaleByWidth != imageScaleByHeight) {
                int gcd = BigInteger.valueOf(defaultWidth).gcd(BigInteger.valueOf(defaultHeight)).intValue();
                throw new IllegalStateException("Texture " + loc + " has invalid aspect ratio, expected " + (defaultWidth / gcd) + ":" + (defaultHeight / gcd));
            }
            this.images.put(loc, Pair.of(image, imageScaleByWidth));
            this.scale = lcm(this.scale, imageScaleByWidth);
        }
        return this;
    }

    /**
     * Creates the resulting {@link Textures} object.
     */
    public Textures build() {
        Map<ResourceLocation, Pair<BufferedImage, Integer>> allImages = new HashMap<>(this.images);
        allImages.putAll(this.fakes);
        return new Textures(this.mod, this.textureLoader, this.scale, allImages);
    }

    private static boolean isPowerOfTwo(int number) {
        return number > 0 && (number & (number - 1)) == 0;
    }

    private static int gcd(int a, int b) {
        return BigInteger.valueOf(a).gcd(BigInteger.valueOf(b)).intValue();
    }

    private static int lcm(int a, int b) {
        return (a * b) / gcd(a, b);
    }
}

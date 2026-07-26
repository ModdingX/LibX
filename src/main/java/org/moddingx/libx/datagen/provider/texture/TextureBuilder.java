package org.moddingx.libx.datagen.provider.texture;

import net.minecraft.resources.Identifier;
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
    private final Function<Identifier, BufferedImage> textureLoader;
    
    private int scale;
    private final Map<Identifier, Pair<BufferedImage, Integer>> images;
    private final Map<Identifier, Pair<BufferedImage, Integer>> fakes;

    public TextureBuilder(ModX mod, Function<Identifier, BufferedImage> textureLoader) {
        this.mod = mod;
        this.textureLoader = textureLoader;
        
        this.scale = 1;
        this.images = new HashMap<>();
        this.fakes = new HashMap<>();
    }

    /**
     * Adds a required texture which has the given size by default as width and height.
     */
    public TextureBuilder addTexture(String id, int defaultSize) {
        return this.addTexture(id, defaultSize, defaultSize);
    }

    /**
     * Adds a required image which has the given size by default as width and height.
     */
    public TextureBuilder addImage(String id, int defaultSize) {
        return this.addImage(id, defaultSize, defaultSize);
    }

    /**
     * Adds a required texture which has the given size by default as width and height.
     */
    public TextureBuilder addTexture(Identifier id, int defaultSize) {
        return this.addTexture(id, defaultSize, defaultSize);
    }

    /**
     * Adds a required image which has the given size by default as width and height.
     */
    public TextureBuilder addImage(Identifier id, int defaultSize) {
        return this.addImage(id, defaultSize, defaultSize);
    }

    /**
     * Adds a required texture which has the given width and height by default.
     */
    public TextureBuilder addTexture(String id, int defaultWidth, int defaultHeight) {
        return this.addTexture(this.mod.id(id), defaultWidth, defaultHeight);
    }

    /**
     * Adds a required image which has the given width and height by default.
     */
    public TextureBuilder addImage(String id, int defaultWidth, int defaultHeight) {
        return this.addImage(this.mod.id(id), defaultWidth, defaultHeight);
    }

    /**
     * Adds a required texture which has the given width and height by default.
     */
    public TextureBuilder addTexture(Identifier id, int defaultWidth, int defaultHeight) {
        return this.addImage(Identifier.fromNamespaceAndPath(id.getNamespace(), "textures/" + id.getPath() + ".png"), defaultWidth, defaultHeight);
    }

    /**
     * Assigns the given id to a fake image.
     *
     * @param id A fake <i>image</i> id.
     */
    public TextureBuilder addFake(Identifier id, BufferedImage image) {
        return this.addFake(id, image, 1);
    }

    /**
     * Assigns the given id to a fake image and scale.
     *
     * @param id A fake <i>image</i> id.
     */
    public TextureBuilder addFake(Identifier id, BufferedImage image, int scale) {
        if (this.fakes.containsKey(id)) throw new IllegalStateException("Duplicate fake texture: " + id);
        this.fakes.put(id, Pair.of(image, scale));
        return this;
    }

    /**
     * Assigns the given id to a fake image that is obtained by transforming another image.
     * This requires that the texture {@code texId} has already been added.
     * 
     * @param id A fake <i>image</i> id.
     */
    public TextureBuilder addFakeTexture(Identifier id, Identifier texId, UnaryOperator<BufferedImage> image) {
        return this.addFakeImage(id, Identifier.fromNamespaceAndPath(texId.getNamespace(), "textures/" + texId.getPath() + ".png"), image);
    }

    /**
     * Assigns the given id to a fake image that is obtained by transforming another image.
     * This requires that the image {@code imgId} has already been added.
     *
     * @param id A fake <i>image</i> id.
     */
    public TextureBuilder addFakeImage(Identifier id, Identifier imgId, UnaryOperator<BufferedImage> image) {
        if (!this.images.containsKey(imgId)) throw new IllegalStateException("Can't add fake transform of non-loaded image: " + imgId);
        if (this.fakes.containsKey(id)) throw new IllegalStateException("Duplicate fake texture: " + id);
        Pair<BufferedImage, Integer> original = this.images.get(imgId);
        this.fakes.put(id, Pair.of(image.apply(original.getLeft()), original.getRight()));
        return this;
    }

    /**
     * Adds a required image which has the given width and height by default.
     */
    public TextureBuilder addImage(Identifier id, int defaultWidth, int defaultHeight) {
        if (!isPowerOfTwo(defaultWidth)) throw new IllegalArgumentException("Invalid default width for texture " + id + ": " + defaultWidth + " is not a power of two.");
        if (!isPowerOfTwo(defaultHeight)) throw new IllegalArgumentException("Invalid default height for texture " + id + ": " + defaultHeight + " is not a power of two.");
        if (!this.images.containsKey(id)) {
            BufferedImage image = this.textureLoader.apply(id);
            if (!isPowerOfTwo(image.getWidth())) throw new IllegalStateException("Invalid texture width for texture " + id + ": " + image.getWidth() + " is not a power of two.");
            if (!isPowerOfTwo(image.getHeight())) throw new IllegalStateException("Invalid texture height for texture " + id + ": " + image.getHeight() + " is not a power of two.");
            if (image.getWidth() < defaultWidth || image.getHeight() < defaultHeight) throw new IllegalStateException("Invalid texture: " + id + ": Image is smaller than default");
            int imageScaleByWidth = image.getWidth() / defaultWidth;
            int imageScaleByHeight = image.getHeight() / defaultHeight;
            if (imageScaleByWidth != imageScaleByHeight) {
                int gcd = BigInteger.valueOf(defaultWidth).gcd(BigInteger.valueOf(defaultHeight)).intValue();
                throw new IllegalStateException("Texture " + id + " has invalid aspect ratio, expected " + (defaultWidth / gcd) + ":" + (defaultHeight / gcd));
            }
            this.images.put(id, Pair.of(image, imageScaleByWidth));
            this.scale = lcm(this.scale, imageScaleByWidth);
        }
        return this;
    }

    /**
     * Creates the resulting {@link Textures} object.
     */
    public Textures build() {
        Map<Identifier, Pair<BufferedImage, Integer>> allImages = new HashMap<>(this.images);
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

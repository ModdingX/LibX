package io.github.noeppi_noeppi.libx.data.provider.texture;

import io.github.noeppi_noeppi.libx.impl.data.texture.SignTextureFactory;
import io.github.noeppi_noeppi.libx.impl.data.texture.TextureGenerator;
import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A provider to generate textures during datagen.
 * 
 * When using this, notice the difference between a <i>texture</i> id and an <i>image</i> id.
 * 
 * A <i>texture</i> id is a {@link ResourceLocation} holding the namespace and the path of a texture
 * as it is used in block and item models.
 * 
 * An <i>image</i> id is a {@link ResourceLocation} in the format that is passed
 * to {@link TextureManager#getTexture(ResourceLocation)}.
 * A <i>texture</i> id is converted to an <i>image</i> id like this: {@code namespace:textures/path.png}.
 * 
 * <h3>Scaling</h3>
 * 
 * This provider takes care that a resulting texture is scaled up as much as required to fit all the
 * textures used to build that texture on it.
 * 
 * Scales are always positive integers as the provider expects textures to have widths and heights that
 * are powers of {@code 2}.
 * 
 * The target scale is defined as the amount that the {@link TextureFactory#getSize() requested size} <b>has
 * been</b> scaled up, so all required texture can fit on it. The image passed
 * in {@link TextureFactory#generate(BufferedImage, Textures)} will always have the requested size times the
 * target scale.
 * 
 * A source image scale is defined as the amount that a source image <b>needs to be</b> scaled up to match the
 * target image.
 * 
 * TO register textures that should be generated, use one of the {@code texture} and {@code image} methods
 * during {@link #setup()}.
 */
public abstract class TextureProviderBase implements DataProvider {

    private final ModX mod;
    private final TextureGenerator generator;
    private final Map<ResourceLocation, TextureFactory> textures;

    protected TextureProviderBase(ModX mod, DataGenerator generator, ExistingFileHelper fileHelper) {
        this.mod = mod;
        this.generator = new TextureGenerator(mod.modid, generator, fileHelper);
        this.textures = new HashMap<>();
    }

    @Nonnull
    @Override
    public String getName() {
        return this.mod.modid + " textures.";
    }

    public abstract void setup();

    /**
     * Adds a texture that should be generated.
     */
    public void texture(String loc, TextureFactory factory) {
        this.texture(this.mod.resource(loc), factory);
    }

    /**
     * Adds an image that should be generated.
     */
    public void image(String loc, TextureFactory factory) {
        this.image(this.mod.resource(loc), factory);
    }

    /**
     * Adds a texture that should be generated.
     */
    public void texture(ResourceLocation loc, TextureFactory factory) {
        this.image(new ResourceLocation(loc.getNamespace(), "textures/" + loc.getPath() + ".png"), factory);
    }

    /**
     * Adds an image that should be generated.
     */
    public void image(ResourceLocation loc, TextureFactory factory) {
        this.textures.put(loc, factory);
    }

    /**
     * Generates a sign texture for the given {@link WoodType} with the given two textures as log and planks.
     */
    public void sign(WoodType wood, ResourceLocation log, ResourceLocation planks) {
        ResourceLocation woodId = new ResourceLocation(wood.name());
        this.sign(new ResourceLocation(woodId.getNamespace(), "entity/signs/" + woodId.getPath()), log, planks);
    }

    /**
     * Generates a sign texture with the given id and the given two textures as log and planks.
     */
    public void sign(ResourceLocation signTexture, ResourceLocation log, ResourceLocation planks) {
        this.texture(signTexture, new SignTextureFactory(log, planks));
    }
    
    @Override
    public void run(@Nonnull HashCache cache) throws IOException {
        this.setup();
        for (Map.Entry<ResourceLocation, TextureFactory> entry : this.textures.entrySet()) {
            ResourceLocation id = entry.getKey();
            TextureFactory factory = entry.getValue();

            TextureBuilder builder = new TextureBuilder(this.mod, this.generator::loadImage);
            factory.addTextures(builder);
            Textures textures = builder.build();

            Dimension dim = factory.getSize();
            BufferedImage image = this.generator.newImage(dim.width, dim.height, textures.scale());
            factory.generate(image, textures);
            this.generator.save(cache, id, image);
        }
    }
}

package org.moddingx.libx.datagen.provider.texture;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.impl.datagen.texture.HangingSignTextureFactory;
import org.moddingx.libx.impl.datagen.texture.SignTextureFactory;
import org.moddingx.libx.impl.datagen.texture.TextureGenerator;
import org.moddingx.libx.mod.ModX;

import javax.annotation.Nonnull;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * A provider to generate textures during datagen.
 * 
 * When using this, notice the difference between a <i>texture</i> id and an <i>image</i> id.
 * 
 * A <i>texture</i> id is a {@link Identifier} holding the namespace and the path of a texture
 * as it is used in block and item models.
 * 
 * An <i>image</i> id is a {@link Identifier} in the format that is passed
 * to {@link TextureManager#getTexture(Identifier)}.
 * A <i>texture</i> id is converted to an <i>image</i> id like this: {@code namespace:textures/path.png}.
 * 
 * <h2>Scaling</h2>
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
 * To register textures that should be generated, use one of the {@code texture} and {@code image} methods
 * during {@link #setup()}.
 */
public abstract class TextureProviderBase implements DataProvider {

    private final ModX mod;
    private final TextureGenerator generator;
    private final Map<Identifier, TextureFactory> textures;

    protected TextureProviderBase(DatagenContext ctx) {
        this.mod = ctx.mod();
        this.generator = new TextureGenerator(ctx.target());
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
    public void texture(String id, TextureFactory factory) {
        this.texture(this.mod.id(id), factory);
    }

    /**
     * Adds an image that should be generated.
     */
    public void image(String id, TextureFactory factory) {
        this.image(this.mod.id(id), factory);
    }

    /**
     * Adds a texture that should be generated.
     */
    public void texture(Identifier id, TextureFactory factory) {
        this.image(Identifier.fromNamespaceAndPath(id.getNamespace(), "textures/" + id.getPath() + ".png"), factory);
    }

    /**
     * Adds an image that should be generated.
     */
    public void image(Identifier id, TextureFactory factory) {
        this.textures.put(id, factory);
    }

    /**
     * Generates a sign texture for the given {@link WoodType} with the given two blocks as log and planks.
     */
    public void sign(WoodType wood, Block log, Block planks) {
        Identifier logId = Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(log));
        Identifier planksId = Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(planks));
        this.sign(wood,
                Identifier.fromNamespaceAndPath(logId.getNamespace(), "block/" + logId.getPath()),
                Identifier.fromNamespaceAndPath(planksId.getNamespace(), "block/" + planksId.getPath())
        );
    }
    
    /**
     * Generates a sign texture for the given {@link WoodType} with the given two textures as log and planks.
     */
    public void sign(WoodType wood, Identifier log, Identifier planks) {
        Identifier woodId = Identifier.parse(wood.name());
        this.sign(Identifier.fromNamespaceAndPath(woodId.getNamespace(), "entity/signs/" + woodId.getPath()), log, planks);
    }

    /**
     * Generates a sign texture with the given id and the given two textures as log and planks.
     */
    public void sign(Identifier signTexture, Identifier log, Identifier planks) {
        this.texture(signTexture, new SignTextureFactory(log, planks));
    }

    /**
     * Generates a hanging sign texture for the given {@link WoodType} with the given block as stripped log.
     */
    public void hangingSign(WoodType wood, Block strippedLog) {
        Identifier logId = Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(strippedLog));
        this.hangingSign(wood, Identifier.fromNamespaceAndPath(logId.getNamespace(), "block/" + logId.getPath()));
    }

    /**
     * Generates a hanging sign texture for the given {@link WoodType} with the given texture as stripped log.
     */
    public void hangingSign(WoodType wood, Identifier strippedLog) {
        Identifier woodId = Identifier.parse(wood.name());
        this.hangingSign(Identifier.fromNamespaceAndPath(woodId.getNamespace(), "entity/signs/hanging/" + woodId.getPath()), strippedLog);
    }

    /**
     * Generates a hanging sign texture with the given id and the given texture as stripped log.
     */
    public void hangingSign(Identifier signTexture, Identifier strippedLog) {
        this.texture(signTexture, new HangingSignTextureFactory(strippedLog));
    }

    @Nonnull
    @Override
    public CompletableFuture<?> run(@Nonnull CachedOutput output) {
        this.setup();
        return CompletableFuture.allOf(this.textures.entrySet().stream().map(entry -> {
            Identifier id = entry.getKey();
            TextureFactory factory = entry.getValue();

            TextureBuilder builder = new TextureBuilder(this.mod, this.generator::loadImage);
            factory.addTextures(builder);
            Textures textures = builder.build();

            Dimension dim = factory.getSize();
            BufferedImage image = this.generator.newImage(dim.width, dim.height, textures.scale());
            factory.generate(image, textures);
            return this.generator.save(output, id, image);
        }).toArray(CompletableFuture[]::new));
    }
}

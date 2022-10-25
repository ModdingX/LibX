package org.moddingx.libx.impl.datagen.texture;

import org.moddingx.libx.datagen.provider.texture.TextureHelper;
import org.moddingx.libx.datagen.provider.texture.TextureBuilder;
import org.moddingx.libx.datagen.provider.texture.TextureFactory;
import org.moddingx.libx.datagen.provider.texture.Textures;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SignTextureFactory implements TextureFactory {

    private final ResourceLocation log;
    private final ResourceLocation planks;

    public SignTextureFactory(ResourceLocation log, ResourceLocation planks) {
        this.log = log;
        this.planks = planks;
    }

    @Override
    public Dimension getSize() {
        return new Dimension(64, 32);
    }

    @Override
    public void addTextures(TextureBuilder builder) {
        builder.addTexture(this.log, 16);
        builder.addTexture(this.planks, 16);
    }

    @Override
    public void generate(BufferedImage image, Textures textures) {
        TextureHelper.copyTexture(image, textures, this.log, 0, 16, 8, 14, 0, 0);
        TextureHelper.copyTexture(image, textures, this.planks, 0, 0, 16, 14, 0, 2);
        TextureHelper.copyTexture(image, textures, this.planks, 16, 0, 16, 14, 0, 2);
        TextureHelper.copyTexture(image, textures, this.planks, 32, 0, 16, 14, 0, 2);
        TextureHelper.copyTexture(image, textures, this.planks, 48, 0, 4, 14, 0, 2);
        TextureHelper.copyTexture(image, textures, this.planks, 2, 14, 4, 2, 6, 3);
        TextureHelper.clear(image, textures, 0, 0, 2, 2);
        TextureHelper.clear(image, textures, 50, 0, 2, 2);
    }
}

package org.moddingx.libx.impl.datagen.texture;

import net.minecraft.resources.Identifier;
import org.moddingx.libx.datagen.provider.texture.TextureBuilder;
import org.moddingx.libx.datagen.provider.texture.TextureFactory;
import org.moddingx.libx.datagen.provider.texture.TextureHelper;
import org.moddingx.libx.datagen.provider.texture.Textures;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

public class SignTextureFactory implements TextureFactory {

    private final Identifier log;
    private final Identifier planks;

    public SignTextureFactory(Identifier log, Identifier planks) {
        this.log = log;
        this.planks = planks;
    }

    @Override
    public Dimension getSize() {
        return new Dimension(32, 32);
    }

    @Override
    public void addTextures(TextureBuilder builder) {
        builder.addTexture(this.log, 16);
        builder.addTexture(this.planks, 16);
    }

    @Override
    public void generate(BufferedImage image, Textures textures) {
        TextureHelper.copyTexture(image, textures, this.planks, 0, 0, 16, 14, 0, 2);
        TextureHelper.copyTexture(image, textures, this.planks, 16, 0, 8, 14, 0, 2);
        TextureHelper.copyTexture(image, textures, this.planks, 24, 2, 2, 12, 0, 2);
        TextureHelper.copyTexture(image, textures, this.planks, 0, 16, 16, 14, 0, 2);
        TextureHelper.copyTexture(image, textures, this.planks, 16, 16, 8, 14, 0, 2);
        TextureHelper.copyTexture(image, textures, this.planks, 24, 16, 2, 12, 2, 2);
        TextureHelper.copyTexture(image, textures, this.log, 28, 0, 4, 14, 0, 0);
        TextureHelper.copyTexture(image, textures, this.log, 28, 16, 4, 14, 4, 0);
        TextureHelper.copyTexture(image, textures, this.planks, 28, 30, 2, 2, 6, 3);
    }
}

package org.moddingx.libx.impl.datagen.texture;

import com.google.common.hash.HashCode;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class TextureGenerator {

    private final DataGenerator generator;
    private final ExistingFileHelper fileHelper;

    public TextureGenerator(DataGenerator generator, ExistingFileHelper fileHelper) {
        this.generator = generator;
        this.fileHelper = fileHelper;
    }

    public void save(CachedOutput output, ResourceLocation id, BufferedImage image) throws IOException {
        Path path = this.generator.getOutputFolder().resolve("assets").resolve(id.getNamespace()).resolve(id.getPath());
        this.save(output, image, path);
    }
    
    public void save(CachedOutput output, BufferedImage image, Path path) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", byteOut);
        byte[] data = byteOut.toByteArray();
        output.writeIfNeeded(path, data, HashCode.fromBytes(data));
    }
    
    public BufferedImage newImage(int width, int height, int scale) {
        return new BufferedImage(width * scale, height * scale, BufferedImage.TYPE_INT_ARGB);
    }
    
    public BufferedImage loadImage(ResourceLocation image) {
        if (!this.fileHelper.exists(image, PackType.CLIENT_RESOURCES)) {
            throw new RuntimeException("Texture does not exists: " + image);
        }
        try (InputStream in = this.fileHelper.getResource(image, PackType.CLIENT_RESOURCES).open()) {
            return ImageIO.read(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load texture: " + image, e);
        }
    }
}

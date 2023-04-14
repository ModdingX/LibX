package org.moddingx.libx.impl.datagen.texture;

import com.google.common.hash.HashCode;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class TextureGenerator {

    private final PackOutput packOutput;
    private final ExistingFileHelper fileHelper;

    public TextureGenerator(PackOutput packOutput, ExistingFileHelper fileHelper) {
        this.packOutput = packOutput;
        this.fileHelper = fileHelper;
    }

    public CompletableFuture<?> save(CachedOutput output, ResourceLocation id, BufferedImage image) {
        Path path = this.packOutput.getOutputFolder().resolve(PackType.CLIENT_RESOURCES.getDirectory()).resolve(id.getNamespace()).resolve(id.getPath());
        return this.save(output, image, path);
    }
    
    public CompletableFuture<?> save(CachedOutput output, BufferedImage image, Path path) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", byteOut);
            byte[] data = byteOut.toByteArray();
            output.writeIfNeeded(path, data, HashCode.fromBytes(data));
            return CompletableFuture.completedFuture(null);
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
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
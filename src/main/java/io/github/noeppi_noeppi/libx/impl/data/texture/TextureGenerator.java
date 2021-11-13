package io.github.noeppi_noeppi.libx.impl.data.texture;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public class TextureGenerator {

    private final String modid;
    private final DataGenerator generator;
    private final ExistingFileHelper fileHelper;

    public TextureGenerator(String modid, DataGenerator generator, ExistingFileHelper fileHelper) {
        this.modid = modid;
        this.generator = generator;
        this.fileHelper = fileHelper;
    }

    public void save(HashCache cache, ResourceLocation id, BufferedImage image) {
        Path path = this.generator.getOutputFolder().resolve("assets").resolve(id.getNamespace()).resolve(id.getPath());
        this.save(cache, image, path);
    }
    
    public void save(HashCache cache, BufferedImage image, Path path) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", byteOut);
            byte[] data = byteOut.toByteArray();
            //noinspection UnstableApiUsage
            String hash = DataProvider.SHA1.hashBytes(data).toString();
            if (!Objects.equals(cache.getHash(path), hash) || !Files.exists(path)) {
                Files.createDirectories(path.getParent());
                OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                out.write(data);
                out.close();
                cache.putNew(path, hash);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save generated texture: " + path);
        }
    }
    
    public BufferedImage newImage(int width, int height, int scale) {
        return new BufferedImage(width * scale, height * scale, BufferedImage.TYPE_INT_ARGB);
    }
    
    public BufferedImage loadImage(ResourceLocation image) {
        if (!this.fileHelper.exists(image, PackType.CLIENT_RESOURCES)) {
            throw new RuntimeException("Texture does not exists: " + image);
        }
        try (Resource res = this.fileHelper.getResource(image, PackType.CLIENT_RESOURCES); 
             InputStream in = res.getInputStream()) {
            return ImageIO.read(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load texture: " + image, e);
        }
    }
}

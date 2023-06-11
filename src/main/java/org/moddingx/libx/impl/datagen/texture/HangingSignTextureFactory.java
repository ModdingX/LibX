package org.moddingx.libx.impl.datagen.texture;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import org.moddingx.libx.LibX;
import org.moddingx.libx.datagen.provider.texture.*;
import org.moddingx.libx.util.lazy.LazyValue;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class HangingSignTextureFactory implements TextureFactory {

    private static final ResourceLocation CHAINS_IMAGE = LibX.getInstance().resource("hanging_chains");
    private static final ResourceLocation ROTATED_LOG_IMAGE = LibX.getInstance().resource("rotated_log");
    
    private static final LazyValue<BufferedImage> CHAINS = new LazyValue<>(() -> {
        try (InputStream in = new ByteArrayInputStream(new byte[]{
                -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 64, 0, 0, 0, 32, 4, 3, 0, 0, 0,
                80, -77, 99, -73, 0, 0, 0, 18, 80, 76, 84, 69, 0, 0, 0, 37, 44, 61, 62, 68, 83, 73, 80, 101, 91, 100,
                124, -1, -1, -1, -32, 50, 122, 120, 0, 0, 0, 1, 116, 82, 78, 83, 0, 64, -26, -40, 102, 0, 0, 0, 1, 98,
                75, 71, 68, 5, -8, 111, -23, -57, 0, 0, 0, 103, 73, 68, 65, 84, 56, -53, -19, -50, -63, 9, -128, 48, 12,
                70, -31, 71, -69, -64, -81, 19, -92, 118, 1, -83, 27, -24, -2, 59, 121, 48, 32, -106, -12, -18, -63,
                -17, -16, 40, 36, -124, 2, 84, -18, -52, -32, -23, -100, -98, 9, 60, -99, -61, 51, 92, -40, 60, 5, 60,
                -99, 22, -28, -91, 4, 121, -104, -4, -79, 47, -124, 38, -93, 25, -112, -110, -60, -128, 4, 84, -27, 50,
                90, 48, 65, -54, 96, 22, 77, -101, 81, 12, -102, 32, 62, -31, -97, 92, 5, 57, -15, -5, 125, -33, 5, 69,
                116, 7, 47, 38, -9, -24, -23, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126
        })) {
            return ImageIO.read(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read hanging sign chain image", e);
        }
    });
    
    private final ResourceLocation strippedLog;

    public HangingSignTextureFactory(ResourceLocation strippedLog) {
        this.strippedLog = strippedLog;
    }

    @Override
    public Dimension getSize() {
        return new Dimension(64, 32);
    }

    @Override
    public void addTextures(TextureBuilder builder) {
        builder.addTexture(this.strippedLog, 16);
        builder.addFake(CHAINS_IMAGE, CHAINS.get());
        builder.addFakeTexture(ROTATED_LOG_IMAGE, this.strippedLog, img -> ImageTransforms.rotate(img, Rotation.COUNTERCLOCKWISE_90));
    }

    @Override
    public void generate(BufferedImage image, Textures textures) {
        TextureHelper.copyImage(image, textures, ROTATED_LOG_IMAGE, 0, 0, 16, 6, 0, 0);
        TextureHelper.copyImage(image, textures, ROTATED_LOG_IMAGE, 16, 0, 16, 6, 0, 0);
        TextureHelper.copyImage(image, textures, ROTATED_LOG_IMAGE, 32, 0, 8, 6, 0, 0);
        TextureHelper.clear(image, textures, 0, 0, 4, 4);
        TextureHelper.clear(image, textures, 36, 0, 4, 4);
        
        TextureHelper.copyImage(image, textures, ROTATED_LOG_IMAGE, 2, 12, 14, 2, 2, 0);
        TextureHelper.copyImage(image, textures, ROTATED_LOG_IMAGE, 16, 12, 14, 2, 0, 0);
        TextureHelper.copyTexture(image, textures, this.strippedLog, 0, 14, 2, 10, 7, 3);
        TextureHelper.copyTexture(image, textures, this.strippedLog, 2, 14, 7, 10, 0, 3);
        TextureHelper.copyTexture(image, textures, this.strippedLog, 9, 14, 7, 10, 9, 3);
        TextureHelper.copyTexture(image, textures, this.strippedLog, 16, 14, 2, 10, 7, 3);
        TextureHelper.copyTexture(image, textures, this.strippedLog, 18, 14, 7, 10, 0, 3);
        TextureHelper.copyTexture(image, textures, this.strippedLog, 25, 14, 7, 10, 9, 3);
        
        TextureHelper.copyImage(image, textures, CHAINS_IMAGE, 0, 0, 64, 32, 0, 0);
    }
}

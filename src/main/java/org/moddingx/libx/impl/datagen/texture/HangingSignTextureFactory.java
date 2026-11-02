package org.moddingx.libx.impl.datagen.texture;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Rotation;
import org.moddingx.libx.LibX;
import org.moddingx.libx.datagen.provider.texture.*;
import org.moddingx.libx.util.lazy.LazyValue;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class HangingSignTextureFactory implements TextureFactory {

    private static final Identifier CHAINS_IMAGE = LibX.getInstance().id("hanging_chains");
    private static final Identifier ROTATED_LOG_IMAGE = LibX.getInstance().id("rotated_log");
    
    private static final LazyValue<BufferedImage> CHAINS = new LazyValue<>(() -> {
        try (InputStream in = new ByteArrayInputStream(new byte[]{
                -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 32, 0, 0, 0, 32, 8, 6, 0,
                0, 0, 115, 122, 122, -12, 0, 0, 1, 36, 73, 68, 65, 84, 120, -38, -19, -108, 63, 110, -125, 48, 20,
                -121, 127, 70, 108, -19, -44, 33, 74, -122, 74, 93, 60, 84, -30, 10, 102, 98, 97, 67, -30, 12,
                -100, 42, 103, 64, -54, -106, -123, -119, 92, -127, -115, 3, 64, 80, -59, 4, -13, -21, -112, -68,
                -44, 32, 66, -34, -48, 84, 29, -4, 45, -128, -12, -39, 126, -1, -80, -62, -107, 48, 74, -23, -27,
                -11, 13, -57, -61, 94, 97, 5, -87, -57, -60, 73, 70, 0, 48, 14, 61, 0, -96, 44, -14, -55, 58, -113,
                95, -34, 63, 62, 37, -5, -119, 61, 14, -10, 120, -40, -85, 113, -24, -47, -76, 29, -54, 34, 87, 58,
                48, 100, 59, -66, -3, -63, 81, 62, 66, -30, 113, -91, 116, 96, -120, -77, -114, -109, -116, 46,
                107, 13, -43, -43, 73, 77, 42, -48, 127, -99, -47, -76, -35, -61, -115, 37, 94, 24, -91, -73, 67,
                -7, 32, 0, -73, -74, -43, -43, 73, -123, 81, 74, -109, 0, 0, 96, -73, -35, -120, 42, -80, -26, 113,
                -49, 117, 96, 104, 105, 78, -54, 34, 87, -74, -29, -39, 101, -107, 84, 64, -30, -51, 7, -19, -98,
                35, 77, -8, 111, -120, -109, -116, -72, 47, -65, -31, -79, 51, -97, -6, 57, -2, 51, 90, 0, 0, 77,
                -37, 65, 7, -122, 118, -37, 13, -22, 106, -102, -64, -30, -99, -96, 3, 35, -54, 76, -22, -83, -83,
                -73, -97, -34, 60, 106, 9, 82, 79, -14, 7, -7, -49, -100, -87, 123, -41, -81, -35, 26, -33, -114,
                76, -110, -103, -44, 27, -121, 126, -15, -22, -3, -71, -96, -32, 112, 56, 28, 14, -121, -61, -31,
                -8, 31, 124, 3, -27, 65, -78, 22, 32, -86, 117, -47, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126
        })) {
            return ImageIO.read(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read hanging sign chain image", e);
        }
    });
    
    private final Identifier strippedLog;

    public HangingSignTextureFactory(Identifier strippedLog) {
        this.strippedLog = strippedLog;
    }

    @Override
    public Dimension getSize() {
        return new Dimension(32, 32);
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
        TextureHelper.copyImage(image, textures, ROTATED_LOG_IMAGE, 16, 4, 4, 2, 0, 4);
        TextureHelper.copyImage(image, textures, ROTATED_LOG_IMAGE, 0, 7, 16, 6, 0, 6);
        TextureHelper.copyImage(image, textures, ROTATED_LOG_IMAGE, 16, 7, 4, 2, 4, 6);

        TextureHelper.copyImage(image, textures, ROTATED_LOG_IMAGE, 2, 14, 14, 2, 2, 0);
        TextureHelper.copyTexture(image, textures, this.strippedLog, 0, 16, 2, 10, 7, 3);
        TextureHelper.copyTexture(image, textures, this.strippedLog, 2, 16, 7, 10, 0, 3);
        TextureHelper.copyTexture(image, textures, this.strippedLog, 9, 16, 7, 10, 9, 3);
        TextureHelper.copyTexture(image, textures, this.strippedLog, 16, 16, 2, 10, 7, 3);
        TextureHelper.copyTexture(image, textures, this.strippedLog, 18, 16, 7, 10, 0, 3);
        TextureHelper.copyTexture(image, textures, this.strippedLog, 25, 16, 7, 10, 9, 3);
        TextureHelper.copyImage(image, textures, ROTATED_LOG_IMAGE, 2, 26, 14, 2, 0, 0);

        TextureHelper.copyImage(image, textures, CHAINS_IMAGE, 0, 0, 32, 32, 0, 0);
    }
}

package org.moddingx.libx.impl.datagen.load;

import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.font.glyphs.SpecialGlyphs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.moddingx.libx.LibX;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;

public class DatagenFontLoader {
    
    private static StringSplitter.WidthProvider widthProvider;
    
    public static StringSplitter.WidthProvider getFontWidthProvider(@Nullable ExistingFileHelper fileHelper) {
        if (widthProvider == null) {
            if (fileHelper == null) throw new RuntimeException("Can't load font without file helper.");
            try {
                Resource res = fileHelper.getResource(new ResourceLocation("minecraft", "font/glyph_sizes.bin"), PackType.CLIENT_RESOURCES);
                byte[] sizes;
                try (InputStream in = res.open()) {
                    sizes = in.readAllBytes();
                }

                widthProvider = (codePoint, style) -> {
                    if (codePoint >= 0 && codePoint < sizes.length) {
                        int data = sizes[codePoint];
                        if (data == 0) return 0;
                        return (data & 0b1111) - ((data >> 4) & 0b1111) + 1;
                    }
                    return SpecialGlyphs.MISSING.getAdvance(style.isBold());
                };
            } catch (IOException e) {
                LibX.logger.error("Failed to load glyph sizes during datagen. Using Missing glyph provider.", e);
                widthProvider = (codePoint, style) -> SpecialGlyphs.MISSING.getAdvance(style.isBold());
            }
        }
        return widthProvider;
    }
}

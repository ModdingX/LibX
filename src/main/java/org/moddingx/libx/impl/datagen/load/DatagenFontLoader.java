package org.moddingx.libx.impl.datagen.load;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.glyphs.SpecialGlyphs;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.moddingx.libx.LibX;
import org.moddingx.libx.impl.reflect.ReflectionHacks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DatagenFontLoader {
    
    // Makes everything zero-width. Useful when splitting strings that have formatting codes.
    public static final ResourceLocation ZERO_WIDTH_FONT = LibX.getInstance().resource("zero_width");
    public static final StringSplitter MISSING = new StringSplitter((cp, style) -> ZERO_WIDTH_FONT.equals(style.getFont()) ? 0 : SpecialGlyphs.MISSING.getAdvance(style.isBold()));
    
    private static StringSplitter fontMetrics;
    
    public static StringSplitter getFontMetrics(@Nullable ExistingFileHelper fileHelper) {
        if (fontMetrics == null) {
            if (fileHelper == null) throw new RuntimeException("Can't load font without file helper.");
            try {
                LibX.logger.info("Loading font metrics during datagen.");
                ResourceManager rm = DatagenLoader.resources(fileHelper, PackType.CLIENT_RESOURCES);
                
                // We can't call the constructor as it would access the render system
                // However, the prepare method does not need any instance fields, so this works
                FontManager mgr = ReflectionHacks.newInstance(FontManager.class);
                FontManager.Preparation preparation = mgr.prepare(rm, Runnable::run).get(0, TimeUnit.NANOSECONDS);

                // Reverse all glyph provider lists as vanilla sorts higher priorities to the end of the list.
                Map<ResourceLocation, List<GlyphProvider>> providerMap = preparation.providers().entrySet().stream().map(entry -> {
                    ResourceLocation fontId = entry.getKey();
                    List<GlyphProvider> list = new ArrayList<>(entry.getValue());
                    Collections.reverse(list);
                    return Map.entry(fontId, Collections.unmodifiableList(list));
                }).collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
                List<GlyphProvider> defaultGlyphProviders = providerMap.getOrDefault(Style.DEFAULT_FONT, List.of());
                fontMetrics = new StringSplitter((cp, style) -> {
                    if (ZERO_WIDTH_FONT.equals(style.getFont())) return 0;
                    for (GlyphProvider provider : providerMap.getOrDefault(style.getFont(), defaultGlyphProviders)) {
                        GlyphInfo glyph = provider.getGlyph(cp);
                        if (glyph != null) return glyph.getAdvance(style.isBold());
                    }
                    return SpecialGlyphs.MISSING.getAdvance(style.isBold());
                });
                LibX.logger.info("Font loading complete.");
            } catch (Exception e) {
                LibX.logger.error("Failed to load font metrics during datagen. Using Missing glyph provider.", e);
                // Must be the MISSING field, used to test whether this was successful
                fontMetrics = MISSING;
            }
        }
        return fontMetrics;
    }
}

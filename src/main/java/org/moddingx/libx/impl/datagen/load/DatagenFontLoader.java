package org.moddingx.libx.impl.datagen.load;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.UnbakedGlyph;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.glyphs.SpecialGlyphs;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.moddingx.libx.LibX;
import org.moddingx.libx.impl.reflect.ReflectionHacks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DatagenFontLoader {

    // Makes everything zero-width. Useful when splitting strings that have formatting codes.
    public static final Identifier ZERO_WIDTH_FONT = LibX.getInstance().id("zero_width");
    public static final FontDescription.Resource ZERO_WIDTH_FONT_DESCRIPTION = new FontDescription.Resource(ZERO_WIDTH_FONT);
    public static final StringSplitter MISSING = new StringSplitter((cp, style) -> ZERO_WIDTH_FONT_DESCRIPTION.equals(style.getFont()) ? 0 : SpecialGlyphs.MISSING.getAdvance(style.isBold()));
    private static final Identifier UNIFONT_PUA_INCLUDE = Identifier.withDefaultNamespace("font/include/unifont_pua.json");

    private static StringSplitter fontMetrics;

    public static StringSplitter getFontMetrics(@Nullable ResourceManager rm) {
        if (fontMetrics == null) {
            if (rm == null) throw new RuntimeException("Can't load font without file helper.");
            try {
                LibX.logger.info("Loading font metrics during datagen.");
                ResourceManager patchedManager = patchFontResources(rm);

                // We can't call the constructor as it would access the render system
                // However, the prepare method does not need any instance fields, so this works
                FontManager mgr = ReflectionHacks.newInstance(FontManager.class);
                FontManager.Preparation preparation = mgr.prepare(patchedManager, Runnable::run).get(0, TimeUnit.NANOSECONDS);

                // Reverse all glyph provider lists as vanilla sorts higher priorities to the end of the list.
                Map<Identifier, List<GlyphProvider.Conditional>> providerMap = preparation.fontSets().entrySet().stream().map(entry -> {
                    Identifier fontId = entry.getKey();
                    List<GlyphProvider.Conditional> list = new ArrayList<>(entry.getValue());
                    Collections.reverse(list);
                    return Map.entry(fontId, Collections.unmodifiableList(list));
                }).collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
                List<GlyphProvider.Conditional> defaultGlyphProviders = providerMap.getOrDefault(Identifier.withDefaultNamespace("default"), List.of());
                fontMetrics = new StringSplitter((cp, style) -> {
                    FontDescription font = style.getFont();
                    if (ZERO_WIDTH_FONT_DESCRIPTION.equals(font)) return 0;
                    Identifier fontId = font instanceof FontDescription.Resource resource ? resource.id() : Identifier.withDefaultNamespace("default");
                    for (GlyphProvider.Conditional conditional : providerMap.getOrDefault(fontId, defaultGlyphProviders)) {
                        if (!conditional.filter().apply(Set.of())) continue;
                        UnbakedGlyph glyph = conditional.provider().getGlyph(cp);
                        if (glyph != null) return glyph.info().getAdvance(style.isBold());
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

    private static ResourceManager patchFontResources(ResourceManager resourceManager) {
        return new ResourceManager() {

            @Nonnull
            @Override
            public Set<String> getNamespaces() {
                return resourceManager.getNamespaces();
            }

            @Nonnull
            @Override
            public Optional<Resource> getResource(@Nonnull Identifier id) {
                return resourceManager.getResource(id).map(resource -> maybePatchResource(id, resource));
            }

            @Nonnull
            @Override
            public List<Resource> getResourceStack(@Nonnull Identifier id) {
                return resourceManager.getResourceStack(id).stream().map(resource -> maybePatchResource(id, resource)).toList();
            }

            @Nonnull
            @Override
            public Map<Identifier, Resource> listResources(@Nonnull String path, @Nonnull Predicate<Identifier> filter) {
                return resourceManager.listResources(path, filter).entrySet().stream().collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> maybePatchResource(entry.getKey(), entry.getValue())
                ));
            }

            @Nonnull
            @Override
            public Map<Identifier, List<Resource>> listResourceStacks(@Nonnull String path, @Nonnull Predicate<Identifier> filter) {
                return resourceManager.listResourceStacks(path, filter).entrySet().stream().collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream().map(resource -> maybePatchResource(entry.getKey(), resource)).toList()
                ));
            }

            @Nonnull
            @Override
            public Stream<PackResources> listPacks() {
                return resourceManager.listPacks();
            }
        };
    }

    private static Resource maybePatchResource(Identifier id, Resource resource) {
        if (!UNIFONT_PUA_INCLUDE.equals(id)) return resource;
        try (Reader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            if (!json.has("providers") || !json.get("providers").isJsonArray()) {
                return resource;
            }

            boolean changed = false;
            JsonArray providers = json.getAsJsonArray("providers");
            for (JsonElement providerElement : providers) {
                if (providerElement.isJsonObject()) {
                    JsonObject provider = providerElement.getAsJsonObject();
                    if (provider.has("type") && "unihex".equals(provider.get("type").getAsString()) && !provider.has("size_overrides")) {
                        provider.add("size_overrides", new JsonArray());
                        changed = true;
                    }
                }
            }

            if (!changed) return resource;

            byte[] patched = json.toString().getBytes(StandardCharsets.UTF_8);
            return new Resource(resource.source(), () -> new ByteArrayInputStream(patched), resource::metadata);
        } catch (Exception e) {
            LibX.logger.warn("Failed to patch {} for datagen font loading: {}", UNIFONT_PUA_INCLUDE, e.getMessage());
            return resource;
        }
    }
}

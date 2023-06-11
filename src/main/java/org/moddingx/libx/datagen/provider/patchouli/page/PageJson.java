package org.moddingx.libx.datagen.provider.patchouli.page;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.StringSplitter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.moddingx.libx.impl.datagen.load.DatagenFontLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Some utilities for creating pages in a patchouli book.
 */
public class PageJson {

    /**
     * Gets a {@link JsonElement} that represents the given {@link ItemStack} in the format, patchouli requires it.
     */
    public static JsonElement stack(ItemStack stack) {
        StringBuilder sb = new StringBuilder();
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) throw new IllegalStateException("Item not registered: " + stack);
        sb.append(id.getNamespace());
        sb.append(":");
        sb.append(id.getPath());
        if (stack.getCount() != 1) {
            sb.append("#");
            sb.append(stack.getCount());
        }
        if (stack.hasTag() && !stack.getOrCreateTag().isEmpty()) {
            sb.append(stack.getOrCreateTag());
        }
        return new JsonPrimitive(sb.toString());
    }

    /**
     * Splits the given text onto multiple full text pages.
     */
    public static List<String> splitText(String text) {
        return splitText(text, false);
    }
    
    /**
     * Splits the given text onto multiple full text pages.
     * 
     * @param withInit Whether the first text page should be a little bit smaller, so there is enough space for the
     *                 entry header.
     */
    public static List<String> splitText(String text, boolean withInit) {
        return splitText(text, withInit ? 14 : 16, 16);
    }

    /**
     * Splits the given text onto multiple text pages.
     * 
     * @param skip How many lines, the first page should be shorter that usual.
     */
    public static List<String> splitText(String text, int skip) {
        return splitText(text, Math.max(16 - skip, 1), 16);
    }
    
    /**
     * Splits the given text onto multiple text pages.
     * 
     * @param linesHead The amount of lines on the first page.
     * @param linesTail The amount of lines on all other pages.
     */
    public static List<String> splitText(String text, int linesHead, int linesTail) {
        Component displayText = displayText(text);
        StringSplitter.WidthProvider width = DatagenFontLoader.getFontWidthProvider(null);
        StringSplitter splitter = new StringSplitter((codePoint, style) -> style.isObfuscated() ? 0 : width.getWidth(codePoint, style));
        List<String> lines = splitter.splitLines(displayText, Math.round(116 * 1.65f), Style.EMPTY).stream().map(FormattedText::getString).map(String::strip).filter(s -> !s.isEmpty()).toList();
        List<String> pages = new ArrayList<>();
        boolean first = true;
        while (!lines.isEmpty()) {
            pages.add(lines.stream().limit(first ? linesHead : linesTail).collect(Collectors.joining(" ")));
            lines = lines.stream().skip(first ? linesHead : linesTail).toList();
            first = false;
        }
        return List.copyOf(pages);
    }
    
    // Make a component, where formatting codes have an obfuscated style, so they can be 0 width in
    // our custom width provider.
    private static Component displayText(String text) {
        Style obfuscated = Style.EMPTY.withObfuscated(true);
        MutableComponent display = Component.empty();
        
        StringBuilder current = new StringBuilder();
        StringBuilder currentFmt = new StringBuilder();
        for (int idx = 0; idx < text.length();) {
            if (text.charAt(idx) == '$' && idx + 1 < text.length() && text.charAt(idx + 1) == '(') {
                if (!current.isEmpty()) {
                    display.append(Component.literal(current.toString()).setStyle(Style.EMPTY));
                    current = new StringBuilder();
                }
                int openCodes = 1;
                currentFmt.append("$(");
                idx += 2;
                while (openCodes > 0 && idx < text.length()) {
                    // Handle nested codes
                    if (text.charAt(idx) == '$' && idx + 1 < text.length() && text.charAt(idx + 1) == '(') {
                        currentFmt.append("$(");
                        openCodes += 1;
                        idx += 2;
                    } else if (text.charAt(idx) == ')') {
                        currentFmt.append(")");
                        openCodes -= 1;
                        idx += 1;
                    } else {
                        currentFmt.append(text.charAt(idx));
                        idx += 1;
                    }
                }
            } else {
                if (!currentFmt.isEmpty()) {
                    display.append(Component.literal(currentFmt.toString()).setStyle(obfuscated));
                    currentFmt = new StringBuilder();
                }
                current.append(text.charAt(idx));
                idx += 1;
            }
        }
        if (!current.isEmpty()) {
            display.append(Component.literal(current.toString()).setStyle(Style.EMPTY));
        }
        if (!currentFmt.isEmpty()) {
            display.append(Component.literal(currentFmt.toString()).setStyle(obfuscated));
        }
        return display;
    }
}

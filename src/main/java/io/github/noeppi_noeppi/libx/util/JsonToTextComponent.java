package io.github.noeppi_noeppi.libx.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.util.Map;

/**
 * Translates JSON into {@link IFormattableTextComponent text components}.
 */
public class JsonToTextComponent {

    private static final HoverEvent COPY_JSON = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("libx.misc.copy_json"));

    /**
     * This translates JSON to a colored text component.
     */
    public static IFormattableTextComponent toText(JsonElement element) {
        Style copyTag = Style.EMPTY.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, element.toString())).setHoverEvent(COPY_JSON);
        return toTextInternal(element).mergeStyle(copyTag);
    }

    private static IFormattableTextComponent toTextInternal(JsonElement element) {
        if (element.isJsonNull()) {
            return new StringTextComponent("");
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return new StringTextComponent(primitive.getAsBoolean() ? "true" : "false").mergeStyle(TextFormatting.GOLD);
            } else if (primitive.isNumber()) {
                return new StringTextComponent(primitive.getAsNumber().toString()).mergeStyle(TextFormatting.GOLD);
            } else if (primitive.isString()) {
                return new StringTextComponent("\"")
                        .appendSibling(new StringTextComponent(escape(primitive.getAsString())).mergeStyle(TextFormatting.GREEN))
                        .appendSibling(new StringTextComponent("\""));
            } else {
                return toTextInternal(primitive);
            }
        } else if (element.isJsonArray()) {
            IFormattableTextComponent tc = new StringTextComponent("[");
            boolean first = true;
            for (JsonElement entry : element.getAsJsonArray()) {
                if (first) {
                    first = false;
                } else {
                    tc.appendSibling(new StringTextComponent(", "));
                }
                tc = tc.appendSibling(toTextInternal(entry));
            }
            tc = tc.appendSibling(new StringTextComponent("]"));
            return tc;
        } else if (element.isJsonObject()) {
            IFormattableTextComponent tc = new StringTextComponent("{");
            boolean first = true;
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                if (first) {
                    first = false;
                } else {
                    tc.appendSibling(new StringTextComponent(", "));
                }
                tc = tc.appendSibling(new StringTextComponent("\"")
                        .appendSibling(new StringTextComponent(entry.getKey()).mergeStyle(TextFormatting.AQUA))
                        .appendSibling(new StringTextComponent("\"")))
                        .appendSibling(new StringTextComponent(": "))
                        .appendSibling(toTextInternal(entry.getValue()));
            }
            tc = tc.appendSibling(new StringTextComponent("}"));
            return tc;
        } else {
            throw new IllegalArgumentException("JSON type unknown: " + element.getClass());
        }
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\\n")
                .replace("\t", "\\\t")
                .replace("\r", "\\\r")
                .replace("\0", "\\\0")
                .replace("\f", "\\\f");
    }
}

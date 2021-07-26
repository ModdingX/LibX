package io.github.noeppi_noeppi.libx.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;

import java.util.Map;

/**
 * Translates JSON into {@link Component text components}.
 */
public class JsonToText {

    private static final HoverEvent COPY_JSON = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("libx.misc.copy_json"));

    /**
     * This translates JSON to a colored text component.
     */
    public static MutableComponent toText(JsonElement element) {
        Style copyTag = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, element.toString())).withHoverEvent(COPY_JSON);
        return toTextInternal(element).withStyle(copyTag);
    }

    private static MutableComponent toTextInternal(JsonElement element) {
        if (element.isJsonNull()) {
            return new TextComponent("");
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return new TextComponent(primitive.getAsBoolean() ? "true" : "false").withStyle(ChatFormatting.GOLD);
            } else if (primitive.isNumber()) {
                return new TextComponent(primitive.getAsNumber().toString()).withStyle(ChatFormatting.GOLD);
            } else if (primitive.isString()) {
                return new TextComponent("\"")
                        .append(new TextComponent(escape(primitive.getAsString())).withStyle(ChatFormatting.GREEN))
                        .append(new TextComponent("\""));
            } else {
                return toTextInternal(primitive);
            }
        } else if (element.isJsonArray()) {
            MutableComponent tc = new TextComponent("[");
            boolean first = true;
            for (JsonElement entry : element.getAsJsonArray()) {
                if (first) {
                    first = false;
                } else {
                    tc.append(new TextComponent(", "));
                }
                tc = tc.append(toTextInternal(entry));
            }
            tc = tc.append(new TextComponent("]"));
            return tc;
        } else if (element.isJsonObject()) {
            MutableComponent tc = new TextComponent("{");
            boolean first = true;
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                if (first) {
                    first = false;
                } else {
                    tc.append(new TextComponent(", "));
                }
                tc = tc.append(new TextComponent("\"")
                        .append(new TextComponent(entry.getKey()).withStyle(ChatFormatting.AQUA))
                        .append(new TextComponent("\"")))
                        .append(new TextComponent(": "))
                        .append(toTextInternal(entry.getValue()));
            }
            tc = tc.append(new TextComponent("}"));
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

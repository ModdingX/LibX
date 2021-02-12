package io.github.noeppi_noeppi.libx.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.util.Map;

/**
 * Translates JSON into text components.
 */
public class JsonToTextComponent {

    private static final HoverEvent COPY_NBT = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("libx.misc.copy_nbt"));

    /**
     * This translates a piece of NBT to a colored text component in JSON style.
     */
    public static IFormattableTextComponent toText(JsonElement element) {
        Style copyTag = Style.EMPTY.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, element.toString())).setHoverEvent(COPY_NBT);
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
                        .append(new StringTextComponent(escape(primitive.getAsString())).mergeStyle(TextFormatting.GREEN))
                        .append(new StringTextComponent("\""));
            } else {
                return toTextInternal(primitive);
            }
        } else if (element.isJsonArray()) {
            return getFromArray((JsonArray) element);
        } else if (element.isJsonObject()) {
            return getFromObject((JsonObject) element);
        } else {
            throw new IllegalArgumentException("JSON type unknown: " + element.getClass());
        }
    }

    private static IFormattableTextComponent getFromArray(JsonArray array) {
        IFormattableTextComponent tc = new StringTextComponent("[");

        for (JsonElement element : array) {
            tc = tc.append(toTextInternal(element));
        }

        tc = tc.append(new StringTextComponent("]"));
        return tc;
    }

    private static IFormattableTextComponent getFromObject(JsonObject object) {
        IFormattableTextComponent tc = new StringTextComponent("{");

        int i = 0;
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            i++;
            tc = tc.append(new StringTextComponent("\"")
                        .append(new StringTextComponent(entry.getKey()).mergeStyle(TextFormatting.AQUA))
                        .append(new StringTextComponent("\"")))
                    .append(new StringTextComponent(": "))
                    .append(toTextInternal(entry.getValue()));
            if (i < object.entrySet().size()) {
                tc = tc.append(new StringTextComponent(", "));
            }
        }

        tc = tc.append(new StringTextComponent("}"));
        return tc;
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

package io.github.noeppi_noeppi.libx.util;

import io.github.noeppi_noeppi.libx.annotation.meta.RemoveIn;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Translates {@link Tag Named Binary Tag} into {@link MutableComponent text components}.
 * 
 * @deprecated Use {@link NbtUtils#toPrettyComponent(Tag)} instead.
 */
@Deprecated(forRemoval = true)
@RemoveIn(minecraft = "1.18.2")
public class NbtToText {

    private static final HoverEvent COPY_NBT = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("libx.misc.copy"));

    public static MutableComponent toText(Tag nbt) {
        Style copyTag = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, nbt.toString())).withHoverEvent(COPY_NBT);
        return toTextInternal(nbt).withStyle(copyTag);
    }

    private static MutableComponent toTextInternal(Tag nbt) {
        if (nbt instanceof EndTag) {
            return new TextComponent("");
        } else if (nbt instanceof CompoundTag tag) {
            MutableComponent tc = new TextComponent("{");
            List<String> keys = tag.getAllKeys().stream().sorted().collect(Collectors.toList());
            for (int i = 0; i < keys.size(); i++) {
                if (quotesRequired(keys.get(i))) {
                    tc = tc.append(new TextComponent("\""));
                }
                tc = tc.append(new TextComponent(escape(keys.get(i))).withStyle(ChatFormatting.AQUA));
                if (quotesRequired(keys.get(i))) {
                    tc = tc.append(new TextComponent("\": "));
                } else {
                    tc = tc.append(new TextComponent(": "));
                }
                tc = tc.append(toTextInternal(tag.get(keys.get(i))));
                if (i + 1 < keys.size()) {
                    tc = tc.append(new TextComponent(", "));
                }
            }
            tc = tc.append(new TextComponent("}"));
            return tc;
        } else if (nbt instanceof ListTag tag) {
            MutableComponent tc = new TextComponent("[");
            for (int i = 0; i < tag.size(); i++) {
                tc = tc.append(toTextInternal(tag.get(i)));
                if (i + 1 < tag.size()) {
                    tc = tc.append(new TextComponent(", "));
                }
            }
            tc = tc.append(new TextComponent("]"));
            return tc;
        } else if (nbt instanceof ByteTag tag) {
            return new TextComponent(Integer.toString(tag.getAsByte())).withStyle(ChatFormatting.GOLD)
                    .append(new TextComponent("b").withStyle(ChatFormatting.RED));
        } else if (nbt instanceof DoubleTag tag) {
            return new TextComponent(Double.toString(tag.getAsDouble())).withStyle(ChatFormatting.GOLD)
                    .append(new TextComponent("d").withStyle(ChatFormatting.RED));
        } else if (nbt instanceof FloatTag tag) {
            return new TextComponent(Float.toString(tag.getAsFloat())).withStyle(ChatFormatting.GOLD)
                    .append(new TextComponent("f").withStyle(ChatFormatting.RED));
        } else if (nbt instanceof IntTag tag) {
            return new TextComponent(Integer.toString(tag.getAsInt())).withStyle(ChatFormatting.GOLD);
        } else if (nbt instanceof LongTag tag) {
            return new TextComponent(Long.toString(tag.getAsLong())).withStyle(ChatFormatting.GOLD)
                    .append(new TextComponent("l").withStyle(ChatFormatting.RED));
        } else if (nbt instanceof ShortTag tag) {
            return new TextComponent(Integer.toString(tag.getAsShort())).withStyle(ChatFormatting.GOLD)
                    .append(new TextComponent("s").withStyle(ChatFormatting.RED));
        } else if (nbt instanceof StringTag tag) {
            return new TextComponent("\"")
                    .append(new TextComponent(escape(tag.getAsString())).withStyle(ChatFormatting.GREEN))
                    .append(new TextComponent("\""));
        } else if (nbt instanceof ByteArrayTag tag) {
            MutableComponent tc = new TextComponent("[");
            tc = tc.append(new TextComponent("B").withStyle(ChatFormatting.LIGHT_PURPLE));
            tc = tc.append(new TextComponent("; "));
            for (int i = 0; i < tag.size(); i++) {
                tc = tc.append(toTextInternal(tag.get(i)));
                if (i + 1 < tag.size()) {
                    tc = tc.append(new TextComponent(", "));
                }
            }
            tc = tc.append(new TextComponent("]"));
            return tc;
        } else if (nbt instanceof IntArrayTag tag) {
            MutableComponent tc = new TextComponent("[");
            tc = tc.append(new TextComponent("I").withStyle(ChatFormatting.LIGHT_PURPLE));
            tc = tc.append(new TextComponent("; "));
            for (int i = 0; i < tag.size(); i++) {
                tc = tc.append(toTextInternal(tag.get(i)));
                if (i + 1 < tag.size()) {
                    tc = tc.append(new TextComponent(", "));
                }
            }
            tc = tc.append(new TextComponent("]"));
            return tc;
        } else if (nbt instanceof LongArrayTag tag) {
            MutableComponent tc = new TextComponent("[");
            tc = tc.append(new TextComponent("L").withStyle(ChatFormatting.LIGHT_PURPLE));
            tc = tc.append(new TextComponent("; "));
            for (int i = 0; i < tag.size(); i++) {
                tc = tc.append(toTextInternal(tag.get(i)));
                if (i + 1 < tag.size()) {
                    tc = tc.append(new TextComponent(", "));
                }
            }
            tc = tc.append(new TextComponent("]"));
            return tc;
        } else {
            throw new IllegalArgumentException("NBT type unknown: " + nbt.getClass());
        }
    }

    private static boolean quotesRequired(String text) {
        for (char chr : text.toCharArray()) {
            if (!Character.isLetterOrDigit(chr) && chr != '_')
                return true;
        }
        return false;
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
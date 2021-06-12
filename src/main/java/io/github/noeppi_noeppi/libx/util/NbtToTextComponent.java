package io.github.noeppi_noeppi.libx.util;

import net.minecraft.nbt.*;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Translates {@link INBT Named Binary Tag} into {@link IFormattableTextComponent text components}.
 */
public class NbtToTextComponent {

    private static final HoverEvent COPY_NBT = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("libx.misc.copy_nbt"));

    /**
     * As the fancy colored {@link NBTTextComponent nbt text components} are only available for
     * {@link NBTTextComponent.Block blocks}, {@link NBTTextComponent.Entity entities}
     * and {@link NBTTextComponent.Storage world storage} by default, this translates a piece of NBT
     * to a colored {@link IFormattableTextComponent text component}.
     */
    public static IFormattableTextComponent toText(INBT nbt) {
        Style copyTag = Style.EMPTY.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, nbt.toString())).setHoverEvent(COPY_NBT);
        return toTextInternal(nbt).mergeStyle(copyTag);
    }

    private static IFormattableTextComponent toTextInternal(INBT nbt) {
        if (nbt instanceof EndNBT) {
            return new StringTextComponent("");
        } else if (nbt instanceof CompoundNBT) {
            IFormattableTextComponent tc = new StringTextComponent("{");
            List<String> keys = ((CompoundNBT) nbt).keySet().stream().sorted().collect(Collectors.toList());
            for (int i = 0; i < keys.size(); i++) {
                if (quotesRequired(keys.get(i))) {
                    tc = tc.appendSibling(new StringTextComponent("\""));
                }
                tc = tc.appendSibling(new StringTextComponent(escape(keys.get(i))).mergeStyle(TextFormatting.AQUA));
                if (quotesRequired(keys.get(i))) {
                    tc = tc.appendSibling(new StringTextComponent("\": "));
                } else {
                    tc = tc.appendSibling(new StringTextComponent(": "));
                }
                tc = tc.appendSibling(toTextInternal(((CompoundNBT) nbt).get(keys.get(i))));
                if (i + 1 < keys.size()) {
                    tc = tc.appendSibling(new StringTextComponent(", "));
                }
            }
            tc = tc.appendSibling(new StringTextComponent("}"));
            return tc;
        } else if (nbt instanceof ListNBT) {
            IFormattableTextComponent tc = new StringTextComponent("[");
            for (int i = 0; i < ((ListNBT) nbt).size(); i++) {
                tc = tc.appendSibling(toTextInternal(((ListNBT) nbt).get(i)));
                if (i + 1 < ((ListNBT) nbt).size()) {
                    tc = tc.appendSibling(new StringTextComponent(", "));
                }
            }
            tc = tc.appendSibling(new StringTextComponent("]"));
            return tc;
        } else if (nbt instanceof ByteNBT) {
            return new StringTextComponent(Integer.toString(((ByteNBT) nbt).getByte())).mergeStyle(TextFormatting.GOLD)
                    .appendSibling(new StringTextComponent("b").mergeStyle(TextFormatting.RED));
        } else if (nbt instanceof DoubleNBT) {
            return new StringTextComponent(Double.toString(((DoubleNBT) nbt).getDouble())).mergeStyle(TextFormatting.GOLD)
                    .appendSibling(new StringTextComponent("d").mergeStyle(TextFormatting.RED));
        } else if (nbt instanceof FloatNBT) {
            return new StringTextComponent(Float.toString(((FloatNBT) nbt).getFloat())).mergeStyle(TextFormatting.GOLD)
                    .appendSibling(new StringTextComponent("f").mergeStyle(TextFormatting.RED));
        } else if (nbt instanceof IntNBT) {
            return new StringTextComponent(Integer.toString(((IntNBT) nbt).getInt())).mergeStyle(TextFormatting.GOLD);
        } else if (nbt instanceof LongNBT) {
            return new StringTextComponent(Long.toString(((LongNBT) nbt).getLong())).mergeStyle(TextFormatting.GOLD)
                    .appendSibling(new StringTextComponent("l").mergeStyle(TextFormatting.RED));
        } else if (nbt instanceof ShortNBT) {
            return new StringTextComponent(Integer.toString(((ShortNBT) nbt).getShort())).mergeStyle(TextFormatting.GOLD)
                    .appendSibling(new StringTextComponent("s").mergeStyle(TextFormatting.RED));
        } else if (nbt instanceof StringNBT) {
            return new StringTextComponent("\"")
                    .appendSibling(new StringTextComponent(escape(nbt.getString())).mergeStyle(TextFormatting.GREEN))
                    .appendSibling(new StringTextComponent("\""));
        } else if (nbt instanceof ByteArrayNBT) {
            IFormattableTextComponent tc = new StringTextComponent("[");
            tc = tc.appendSibling(new StringTextComponent("B").mergeStyle(TextFormatting.LIGHT_PURPLE));
            tc = tc.appendSibling(new StringTextComponent("; "));
            for (int i = 0; i < ((ByteArrayNBT) nbt).size(); i++) {
                tc = tc.appendSibling(toTextInternal(((ByteArrayNBT) nbt).get(i)));
                if (i + 1 < ((ByteArrayNBT) nbt).size()) {
                    tc = tc.appendSibling(new StringTextComponent(", "));
                }
            }
            tc = tc.appendSibling(new StringTextComponent("]"));
            return tc;
        } else if (nbt instanceof IntArrayNBT) {
            IFormattableTextComponent tc = new StringTextComponent("[");
            tc = tc.appendSibling(new StringTextComponent("I").mergeStyle(TextFormatting.LIGHT_PURPLE));
            tc = tc.appendSibling(new StringTextComponent("; "));
            for (int i = 0; i < ((IntArrayNBT) nbt).size(); i++) {
                tc = tc.appendSibling(toTextInternal(((IntArrayNBT) nbt).get(i)));
                if (i + 1 < ((IntArrayNBT) nbt).size()) {
                    tc = tc.appendSibling(new StringTextComponent(", "));
                }
            }
            tc = tc.appendSibling(new StringTextComponent("]"));
            return tc;
        } else if (nbt instanceof LongArrayNBT) {
            IFormattableTextComponent tc = new StringTextComponent("[");
            tc = tc.appendSibling(new StringTextComponent("L").mergeStyle(TextFormatting.LIGHT_PURPLE));
            tc = tc.appendSibling(new StringTextComponent("; "));
            for (int i = 0; i < ((LongArrayNBT) nbt).size(); i++) {
                tc = tc.appendSibling(toTextInternal(((LongArrayNBT) nbt).get(i)));
                if (i + 1 < ((LongArrayNBT) nbt).size()) {
                    tc = tc.appendSibling(new StringTextComponent(", "));
                }
            }
            tc = tc.appendSibling(new StringTextComponent("]"));
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

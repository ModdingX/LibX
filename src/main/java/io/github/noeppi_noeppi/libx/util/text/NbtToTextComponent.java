package io.github.noeppi_noeppi.libx.util.text;

import net.minecraft.nbt.*;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Translates Named Binary Tag into text components.
 */
public class NbtToTextComponent {

    /**
     * As teh fancy colored nbt text components are only availablefor blocks, entities
     * and world storage by default, this translates a piece of NBT to a colored text
     * component.
     */
    public static IFormattableTextComponent toText(INBT nbt) {
        if (nbt instanceof EndNBT) {
            return new StringTextComponent("");
        } else if (nbt instanceof CompoundNBT) {
            IFormattableTextComponent tc = new StringTextComponent("{");
            List<String> keys = ((CompoundNBT) nbt).keySet().stream().sorted().collect(Collectors.toList());
            for (int i = 0;i < keys.size(); i++) {
                if (quotesRequired(keys.get(i))) {
                    tc = tc.append(new StringTextComponent("\""));
                }
                tc = tc.append(new StringTextComponent(escape(keys.get(i))).mergeStyle(TextFormatting.AQUA));
                if (quotesRequired(keys.get(i))) {
                    tc = tc.append(new StringTextComponent("\": "));
                } else {
                    tc = tc.append(new StringTextComponent(": "));
                }
                tc = tc.append(toText(((CompoundNBT) nbt).get(keys.get(i))));
                if (i + 1 < keys.size()) {
                    tc = tc.append(new StringTextComponent(", "));
                }
            }
            tc = tc.append(new StringTextComponent("}"));
            return tc;
        } else if (nbt instanceof ListNBT) {
            IFormattableTextComponent tc = new StringTextComponent("[");
            for (int i = 0; i < ((ListNBT) nbt).size(); i++) {
                tc = tc.append(toText(((ListNBT) nbt).get(i)));
                if (i + 1 < ((ListNBT) nbt).size()) {
                    tc = tc.append(new StringTextComponent(", "));
                }
            }
            tc = tc.append(new StringTextComponent("]"));
            return tc;
        } else if (nbt instanceof ByteNBT) {
            return new StringTextComponent(Integer.toString(((ByteNBT) nbt).getByte())).mergeStyle(TextFormatting.GOLD)
                    .append(new StringTextComponent("b").mergeStyle(TextFormatting.RED));
        } else if (nbt instanceof DoubleNBT) {
            return new StringTextComponent(Double.toString(((DoubleNBT) nbt).getDouble())).mergeStyle(TextFormatting.GOLD)
                    .append(new StringTextComponent("d").mergeStyle(TextFormatting.RED));
        } else if (nbt instanceof FloatNBT) {
            return new StringTextComponent(Float.toString(((FloatNBT) nbt).getFloat())).mergeStyle(TextFormatting.GOLD)
                    .append(new StringTextComponent("f").mergeStyle(TextFormatting.RED));
        } else if (nbt instanceof IntNBT) {
            return new StringTextComponent(Integer.toString(((IntNBT) nbt).getInt())).mergeStyle(TextFormatting.GOLD);
        } else if (nbt instanceof LongNBT) {
            return new StringTextComponent(Long.toString(((LongNBT) nbt).getLong())).mergeStyle(TextFormatting.GOLD)
                    .append(new StringTextComponent("l").mergeStyle(TextFormatting.RED));
        } else if (nbt instanceof ShortNBT) {
            return new StringTextComponent(Integer.toString(((ShortNBT) nbt).getShort())).mergeStyle(TextFormatting.GOLD)
                    .append(new StringTextComponent("s").mergeStyle(TextFormatting.RED));
        } else if (nbt instanceof StringNBT) {
            return new StringTextComponent("\"")
                    .append(new StringTextComponent(escape(nbt.getString())).mergeStyle(TextFormatting.GREEN))
                    .append(new StringTextComponent("\""));
        } else if (nbt instanceof ByteArrayNBT) {
            IFormattableTextComponent tc = new StringTextComponent("[");
            tc = tc.append(new StringTextComponent("B").mergeStyle(TextFormatting.LIGHT_PURPLE));
            tc = tc.append(new StringTextComponent("; "));
            for (int i = 0; i < ((ByteArrayNBT) nbt).size(); i++) {
                tc = tc.append(toText(((ByteArrayNBT) nbt).get(i)));
                if (i + 1 < ((ByteArrayNBT) nbt).size()) {
                    tc = tc.append(new StringTextComponent(", "));
                }
            }
            tc = tc.append(new StringTextComponent("]"));
            return tc;
        } else if (nbt instanceof IntArrayNBT) {
            IFormattableTextComponent tc = new StringTextComponent("[");
            tc = tc.append(new StringTextComponent("I").mergeStyle(TextFormatting.LIGHT_PURPLE));
            tc = tc.append(new StringTextComponent("; "));
            for (int i = 0; i < ((IntArrayNBT) nbt).size(); i++) {
                tc = tc.append(toText(((IntArrayNBT) nbt).get(i)));
                if (i + 1 < ((IntArrayNBT) nbt).size()) {
                    tc = tc.append(new StringTextComponent(", "));
                }
            }
            tc = tc.append(new StringTextComponent("]"));
            return tc;
        } else if (nbt instanceof LongArrayNBT) {
            IFormattableTextComponent tc = new StringTextComponent("[");
            tc = tc.append(new StringTextComponent("L").mergeStyle(TextFormatting.LIGHT_PURPLE));
            tc = tc.append(new StringTextComponent("; "));
            for (int i = 0; i < ((LongArrayNBT) nbt).size(); i++) {
                tc = tc.append(toText(((LongArrayNBT) nbt).get(i)));
                if (i + 1 < ((LongArrayNBT) nbt).size()) {
                    tc = tc.append(new StringTextComponent(", "));
                }
            }
            tc = tc.append(new StringTextComponent("]"));
            return tc;
        } else  {
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
                .replace("\n", "\\\n");
    }
}

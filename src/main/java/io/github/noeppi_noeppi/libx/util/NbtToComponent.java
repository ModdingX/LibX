package io.github.noeppi_noeppi.libx.util;

import net.minecraft.nbt.*;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * Translates {@link INBT Named Binary Tag} into {@link IFormattableTextComponent text components}.
 */
public class NbtToComponent {

    private static final HoverEvent COPY_NBT = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("libx.misc.copy_nbt"));

    /**
     * As the fancy colored {@link NBTTextComponent nbt text components} are only available for
     * {@link NBTTextComponent.Block blocks}, {@link NBTTextComponent.Entity entities}
     * and {@link NBTTextComponent.Storage world storage} by default, this translates a piece of NBT
     * to a colored {@link IFormattableTextComponent text component}.
     */
    public static MutableComponent toText(Tag nbt) {
        Style copyTag = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, nbt.toString())).withHoverEvent(COPY_NBT);
        return toTextInternal(nbt).withStyle(copyTag);
    }

    private static MutableComponent toTextInternal(Tag nbt) {
        if (nbt instanceof EndTag) {
            return new TextComponent("");
        } else if (nbt instanceof CompoundTag) {
            MutableComponent tc = new TextComponent("{");
            List<String> keys = ((CompoundTag) nbt).getAllKeys().stream().sorted().collect(Collectors.toList());
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
                tc = tc.append(toTextInternal(((CompoundTag) nbt).get(keys.get(i))));
                if (i + 1 < keys.size()) {
                    tc = tc.append(new TextComponent(", "));
                }
            }
            tc = tc.append(new TextComponent("}"));
            return tc;
        } else if (nbt instanceof ListTag) {
            MutableComponent tc = new TextComponent("[");
            for (int i = 0; i < ((ListTag) nbt).size(); i++) {
                tc = tc.append(toTextInternal(((ListTag) nbt).get(i)));
                if (i + 1 < ((ListTag) nbt).size()) {
                    tc = tc.append(new TextComponent(", "));
                }
            }
            tc = tc.append(new TextComponent("]"));
            return tc;
        } else if (nbt instanceof ByteTag) {
            return new TextComponent(Integer.toString(((ByteTag) nbt).getAsByte())).withStyle(ChatFormatting.GOLD)
                    .append(new TextComponent("b").withStyle(ChatFormatting.RED));
        } else if (nbt instanceof DoubleTag) {
            return new TextComponent(Double.toString(((DoubleTag) nbt).getAsDouble())).withStyle(ChatFormatting.GOLD)
                    .append(new TextComponent("d").withStyle(ChatFormatting.RED));
        } else if (nbt instanceof FloatTag) {
            return new TextComponent(Float.toString(((FloatTag) nbt).getAsFloat())).withStyle(ChatFormatting.GOLD)
                    .append(new TextComponent("f").withStyle(ChatFormatting.RED));
        } else if (nbt instanceof IntTag) {
            return new TextComponent(Integer.toString(((IntTag) nbt).getAsInt())).withStyle(ChatFormatting.GOLD);
        } else if (nbt instanceof LongTag) {
            return new TextComponent(Long.toString(((LongTag) nbt).getAsLong())).withStyle(ChatFormatting.GOLD)
                    .append(new TextComponent("l").withStyle(ChatFormatting.RED));
        } else if (nbt instanceof ShortTag) {
            return new TextComponent(Integer.toString(((ShortTag) nbt).getAsShort())).withStyle(ChatFormatting.GOLD)
                    .append(new TextComponent("s").withStyle(ChatFormatting.RED));
        } else if (nbt instanceof StringTag) {
            return new TextComponent("\"")
                    .append(new TextComponent(escape(nbt.getAsString())).withStyle(ChatFormatting.GREEN))
                    .append(new TextComponent("\""));
        } else if (nbt instanceof ByteArrayTag) {
            MutableComponent tc = new TextComponent("[");
            tc = tc.append(new TextComponent("B").withStyle(ChatFormatting.LIGHT_PURPLE));
            tc = tc.append(new TextComponent("; "));
            for (int i = 0; i < ((ByteArrayTag) nbt).size(); i++) {
                tc = tc.append(toTextInternal(((ByteArrayTag) nbt).get(i)));
                if (i + 1 < ((ByteArrayTag) nbt).size()) {
                    tc = tc.append(new TextComponent(", "));
                }
            }
            tc = tc.append(new TextComponent("]"));
            return tc;
        } else if (nbt instanceof IntArrayTag) {
            MutableComponent tc = new TextComponent("[");
            tc = tc.append(new TextComponent("I").withStyle(ChatFormatting.LIGHT_PURPLE));
            tc = tc.append(new TextComponent("; "));
            for (int i = 0; i < ((IntArrayTag) nbt).size(); i++) {
                tc = tc.append(toTextInternal(((IntArrayTag) nbt).get(i)));
                if (i + 1 < ((IntArrayTag) nbt).size()) {
                    tc = tc.append(new TextComponent(", "));
                }
            }
            tc = tc.append(new TextComponent("]"));
            return tc;
        } else if (nbt instanceof LongArrayTag) {
            MutableComponent tc = new TextComponent("[");
            tc = tc.append(new TextComponent("L").withStyle(ChatFormatting.LIGHT_PURPLE));
            tc = tc.append(new TextComponent("; "));
            for (int i = 0; i < ((LongArrayTag) nbt).size(); i++) {
                tc = tc.append(toTextInternal(((LongArrayTag) nbt).get(i)));
                if (i + 1 < ((LongArrayTag) nbt).size()) {
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

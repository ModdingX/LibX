package io.github.noeppi_noeppi.libx.util;

import java.util.function.BiConsumer;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.KeybindComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * Utilities for {@link ITextComponent text components}.
 */
public class TextComponentUtil {

    /**
     * Gets a {@link ITextComponent text component} as a string formatted with ANSI escape codes to
     * be printed on the console.
     */
    public static String getConsoleString(Component tc) {
        StringBuilder sb = new StringBuilder();
        traverseComponent(tc, Style.EMPTY, (string, style) -> {
            reset(sb);
            formattingCodes(sb, style);
            sb.append(string);
        });
        reset(sb);
        return sb.toString();
    }
    
    private static void formattingCodes(StringBuilder sb, Style style) {
        if (style.getColor() != null) {
            int color = style.getColor().value;
            sb.append("\u001B[38;2;").append((color >> 16) & 0xFF).append(";").append((color >> 8) & 0xFF).append(";").append(color & 0xFF).append("m");
        }
        if (style.bold != null) {
            if (style.bold) {
                sb.append("\u001B[1m");
            } else {
                sb.append("\u001B[22m");
            }
        }
        if (style.italic != null) {
            if (style.italic) {
                sb.append("\u001B[3m");
            } else {
                sb.append("\u001B[23m");
            }
        }
        if (style.underlined != null) {
            if (style.underlined) {
                sb.append("\u001B[4m");
            } else {
                sb.append("\u001B[24m");
            }
        }
        if (style.strikethrough != null) {
            if (style.strikethrough) {
                sb.append("\u001B[9m");
            } else {
                sb.append("\u001B[29m");
            }
        }
        if (style.obfuscated != null) {
            if (style.obfuscated) {
                sb.append("\u001B[8m");
            } else {
                sb.append("\u001B[28m");
            }
        }
    }
    
    private static void reset(StringBuilder sb) {
        sb.append("\u001B[0m");
    }
    
    // We partially recreate getComponentWithStyle here as it's client only
    private static void traverseComponent(FormattedText tc, Style parent, BiConsumer<String, Style> consumer) {
        Style style = tc instanceof Component ? ((Component) tc).getStyle().applyTo(parent) : parent;
        consumeComponent(tc, style, consumer);
        if (tc instanceof Component) {
            for (Component sibling : ((Component) tc).getSiblings()) {
                traverseComponent(sibling, style, consumer);
            }
        }
    }
    
    private static void consumeComponent(FormattedText tc, Style style, BiConsumer<String, Style> consumer) {
        if (tc instanceof TranslatableComponent) {
            ((TranslatableComponent) tc).decompose();
            for (FormattedText child : ((TranslatableComponent) tc).decomposedParts) {
                traverseComponent(child, style, consumer);
            }
        } else if (tc instanceof KeybindComponent) {
            traverseComponent(((KeybindComponent) tc).getNestedComponent(), style, consumer);
        } else if (tc instanceof Component) {
            consumer.accept(((Component) tc).getContents(), style);
        } else {
            consumer.accept(tc.getString(), style);
        }
    }
}

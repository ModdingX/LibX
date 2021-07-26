package io.github.noeppi_noeppi.libx.util;

import net.minecraft.network.chat.*;

import java.util.Optional;

/**
 * Utilities for {@link Component text components}.
 */
public class ComponentUtil {

    /**
     * Gets a {@link Component text component} as a string formatted with ANSI escape codes to
     * be printed on the console.
     */
    public static String getConsoleString(Component tc) {
        StringBuilder sb = new StringBuilder();
        tc.visit((style, string) -> {
            reset(sb);
            formattingCodes(sb, style);
            sb.append(string);
            return Optional.empty();
        }, Style.EMPTY);
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
}

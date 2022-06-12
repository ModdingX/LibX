package org.moddingx.libx.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Allows text in the lang file to define style information. This is done by dollar commands.
 * A dollar command looks like this: {@code $(...)} where the parts in the parens are multiple
 * commands separated by {@code ;}.
 * A command can be one of these:
 *
 * <ul>
 *     <li>{@code n} or {@code newline}: A newline. When this is used, no other commands can be used in the same pair of parens.</li>
 *     <li>{@code #RRGGBB}</li>: A color in hex
 *     <li>{@code b} or {@code bold}: Bold text.</li>
 *     <li>{@code i} or {@code italic}: Italic text.</li>
 *     <li>{@code u} or {@code underline}: Underlined text.</li>
 *     <li>{@code s} or {@code strikethrough}: Strikethrough text.</li>
 * </ul>
 * 
 * Subclasses can also add custom commands with {@link #customCommand(Style, String)}.
 */
public class TextProcessor {

    /**
     * A text processor without any custom command.
     */
    public static final TextProcessor INSTANCE = new TextProcessor();

    private static final Pattern CONTROL_PATTERN = Pattern.compile("\\$\\(((?:(?:\\w+|#[0-9A-Fa-f]{6})(?:;(?:\\w+|#[0-9A-Fa-f]{6}))*)?)\\)");

    /**
     * Processes a text component.
     * 
     * @return A list of components. Each entry represents one line in the text.
     */
    public List<Component> process(Component text) {
        return this.process(text.getString());
    }

    /**
     * Processes a string.
     *
     * @return A list of components. Each entry represents one line in the text.
     */
    public List<Component> process(String text) {
        return Arrays.stream(text.split("\\$\\((?:n|newline)\\)"))
                .map(String::trim)
                .map(this::processLine)
                .collect(ImmutableList.toImmutableList());
    }
    
    /**
     * Processes a single-line text component.
     */
    public Component processLine(Component line) {
        return this.processLine(line.getString());
    }

    /**
     * Processes a single-line string.
     */
    public Component processLine(String line) {
        if (line.isEmpty()) return new TextComponent("");
        Matcher m = CONTROL_PATTERN.matcher(line);
        MutableComponent tc = new TextComponent("");
        int idx = 0;
        Style style = Style.EMPTY;
        while (m.find()) {
            if (idx < m.start()) {
                tc.append(new TextComponent(line.substring(idx, m.start())).withStyle(style));
            }
            idx = m.end();
            String cmd = m.group(1).trim();
            if (cmd.isEmpty()) {
                style = Style.EMPTY;
            } else {
                for (String part : cmd.split(";")) {
                    if (part.startsWith("#")) {
                        try {
                            int color = Integer.parseInt(part.substring(1), 16);
                            style = style.withColor(TextColor.fromRgb(color));
                        } catch (NumberFormatException e) {
                            //
                        }
                    } else if ("b".equalsIgnoreCase(part.strip()) || "bold".equalsIgnoreCase(part.strip())) {
                        style = style.withBold(true);
                    } else if ("i".equalsIgnoreCase(part.strip()) || "italic".equalsIgnoreCase(part.strip())) {
                        style = style.withItalic(true);
                    } else if ("u".equalsIgnoreCase(part.strip()) || "underline".equalsIgnoreCase(part.strip())) {
                        style = style.withUnderlined(true);
                    } else if ("s".equalsIgnoreCase(part.strip()) || "strikethrough".equalsIgnoreCase(part.strip())) {
                        style = style.setStrikethrough(true);
                    } else {
                        last: {
                            for (ChatFormatting tf : ChatFormatting.values()) {
                                if (tf.getName().equalsIgnoreCase(part.strip())) {
                                    style = style.withColor(tf);
                                    break last;
                                }
                            }
                            style = this.customCommand(style, part.strip());
                        }
                    }
                }
            }
        }
        if (idx < line.length()) {
            tc.append(new TextComponent(line.substring(idx)).withStyle(style));
        }
        return tc;
    }

    /**
     * Handles a custom command. The given style can be modified, or a completely new style can be created
     * The default implementation does nothing.
     */
    public Style customCommand(Style style, String command) {
        return style;
    }
}

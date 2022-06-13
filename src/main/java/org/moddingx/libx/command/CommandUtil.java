package org.moddingx.libx.command;

import com.mojang.brigadier.context.CommandContext;

/**
 * Utilities for commands.
 */
public class CommandUtil {

    /**
     * Gets an argument for a command and if it's not present a default value.
     * 
     * @param name The argument's name
     * @param clazz The argument's class
     * @param defaultValue A default value when the argument is not present.
     */
    public static <T> T getArgumentOrDefault(CommandContext<?> ctx, String name, Class<T> clazz, T defaultValue) {
        try {
            return ctx.getArgument(name, clazz);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }
}

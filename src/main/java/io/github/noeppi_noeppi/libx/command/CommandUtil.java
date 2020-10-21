package io.github.noeppi_noeppi.libx.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.command.arguments.IArgumentSerializer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Method;

public class CommandUtil {

    /**
     * As the normal register method in ArgumentTypes does not work with
     * generic arguments because of javas bad type inference here you can
     * register your generic argument types. Just make sure you don't
     * register something that breaks type safety.
     */
    public static void registerGenericCommandArgument(String name, Class<?> clazz, IArgumentSerializer<?> ias) {
        try {
            Method method = ObfuscationReflectionHelper.findMethod(ArgumentTypes.class, "func_218136_a", String.class, Class.class, IArgumentSerializer.class);
            method.setAccessible(true);
            method.invoke(null, name, clazz, ias);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets an argument for a command and if it's not present a default value.
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

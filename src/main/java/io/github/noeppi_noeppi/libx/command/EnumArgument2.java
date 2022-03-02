package io.github.noeppi_noeppi.libx.command;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.noeppi_noeppi.libx.LibX;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.server.command.EnumArgument;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * As the {@link EnumArgument} is sometimes a bit buggy because when the entered value is
 * lowercase but the actual enum value is uppercase and it also does not sync properly, here's
 * a variant that solves these issues.
 */
public class EnumArgument2<T extends Enum<T>> implements ArgumentType<T> {

    /**
     * Creates a new enum argument for the given enum class.
     */
    public static <R extends Enum<R>> EnumArgument2<R> enumArgument(Class<R> enumClass) {
        return new EnumArgument2<>(enumClass);
    }
    
    private final Class<T> enumClass;
    private final DynamicCommandExceptionType invalidValue;

    private EnumArgument2(final Class<T> enumClass) {
        if (!enumClass.isEnum()) {
            throw new IllegalArgumentException("Can't create enum argument for non-enum class.");
        }
        this.enumClass = enumClass;
        this.invalidValue = new DynamicCommandExceptionType((name) -> new TranslatableComponent("libx.command.argument.enum.invalid", enumClass.getSimpleName(), name));
    }

    @Override
    public T parse(final StringReader reader) throws CommandSyntaxException {
        String str = reader.readUnquotedString().toLowerCase(Locale.ROOT);
        for (T t : this.enumClass.getEnumConstants()) {
            if (t.name().toLowerCase(Locale.ROOT).equals(str)) {
                return t;
            }
        }
        throw this.invalidValue.createWithContext(reader, str);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(Stream.of(this.enumClass.getEnumConstants()).map(Enum::name).map(str -> str.toLowerCase(Locale.ROOT)), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return Stream.of(this.enumClass.getEnumConstants()).map(Enum::name).map(str -> str.toLowerCase(Locale.ROOT)).collect(Collectors.toList());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static class Serializer implements ArgumentSerializer<EnumArgument2<?>> {

        @Override
        public void serializeToNetwork(EnumArgument2 argument, FriendlyByteBuf buffer) {
            buffer.writeUtf(argument.enumClass.getName());
        }

        @Nonnull
        @Override
        public EnumArgument2 deserializeFromNetwork(FriendlyByteBuf buffer) {
            String name = buffer.readUtf();
            try {
                return new EnumArgument2(Class.forName(name));
            } catch (ClassNotFoundException e) {
                LibX.getInstance().logger.warn("Can't get enum value of type " + name + "in command argument. " + e.getMessage());
                //noinspection ConstantConditions
                return null;
            }
        }

        @Override
        public void serializeToJson(EnumArgument2 argument, JsonObject json) {
            json.addProperty("enum", argument.enumClass.getName());
        }
    }
}

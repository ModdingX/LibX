package org.moddingx.libx.command;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraftforge.server.command.EnumArgument;
import org.moddingx.libx.LibX;

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
        this.invalidValue = new DynamicCommandExceptionType((name) -> Component.translatable("libx.command.argument.enum.invalid", enumClass.getSimpleName(), name));
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
    public static class Info implements ArgumentTypeInfo<EnumArgument2<?>, Info.Template> {

        public static final Info INSTANCE = new Info();

        private Info() {

        }

        @Override
        public void serializeToNetwork(@Nonnull Template template, @Nonnull FriendlyByteBuf buffer) {
            buffer.writeUtf(template.enumClass.getName());
        }

        @Nonnull
        @Override
        public Template deserializeFromNetwork(@Nonnull FriendlyByteBuf buffer) {
            String name = buffer.readUtf();
            try {
                Class<?> cls = Class.forName(name);
                if (cls.isEnum()) {
                    return new Template((Class<? extends Enum<?>>) cls);
                } else {
                    LibX.logger.warn("Can't get enum value of type " + name + " in command argument: No enum");
                    return new Template(Unit.class);
                }
            } catch (ClassNotFoundException e) {
                LibX.logger.warn("Can't get enum value of type " + name + " in command argument: " + e.getMessage());
                return new Template(Unit.class);
            }
        }

        @Override
        public void serializeToJson(@Nonnull Template template, @Nonnull JsonObject json) {
            json.addProperty("enum", template.enumClass.getName());
        }

        @Nonnull
        @Override
        public Template unpack(@Nonnull EnumArgument2<?> arg) {
            return new Template(arg.enumClass);
        }

        public class Template implements ArgumentTypeInfo.Template<EnumArgument2<?>> {

            final Class<? extends Enum<?>> enumClass;

            private Template(Class<? extends Enum<?>> enumClass) {
                this.enumClass = enumClass;
            }

            @Nonnull
            @Override
            @SuppressWarnings({"unchecked", "rawtypes"})
            public EnumArgument2<?> instantiate(@Nonnull CommandBuildContext ctx) {
                return new EnumArgument2<>((Class) this.enumClass);
            }

            @Nonnull
            @Override
            public ArgumentTypeInfo<EnumArgument2<?>, ?> type() {
                return Info.this;
            }
        }
    }
}

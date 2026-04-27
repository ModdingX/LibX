package org.moddingx.libx.impl.config.mappers.special;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.moddingx.libx.config.correct.ConfigCorrection;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;

import java.util.*;
import java.util.stream.Collectors;

public class EnumValueMapper<T extends Enum<T>> implements ValueMapper<T, JsonPrimitive> {
    
    private static final Map<Class<? extends Enum<?>>, EnumValueMapper<?>> mappers = new HashMap<>();
    
    public static <T extends Enum<T>> EnumValueMapper<T> getMapper(Class<T> enumClass) {
        if (!enumClass.isEnum()) {
            throw new IllegalArgumentException("Can't get enum serializer for non-enum class: " + enumClass);
        } else if (enumClass.getEnumConstants().length == 0) {
            throw new IllegalArgumentException("Can't get enum serializer for empty enum: " + enumClass);
        } else if (mappers.containsKey(enumClass)) {
            //noinspection unchecked
            return (EnumValueMapper<T>) mappers.get(enumClass);
        } else {
            EnumValueMapper<T> mapper = new EnumValueMapper<>(enumClass);
            mappers.put(enumClass, mapper);
            return mapper;
        }
    }
    
    private final Class<T> cls;

    public EnumValueMapper(Class<T> cls) {
        this.cls = cls;
    }

    @Override
    public Class<T> type() {
        return this.cls;
    }

    @Override
    public Class<JsonPrimitive> element() {
        return JsonPrimitive.class;
    }

    @Override
    public T fromJson(JsonPrimitive json) {
        String str = json.getAsString().toLowerCase(Locale.ROOT).strip();
        T[] enums = this.cls.getEnumConstants();
        for (T e : enums) {
            if (e.name().toLowerCase(Locale.ROOT).equals(str)) {
                return e;
            }
        }
        throw new NoSuchElementException("Enum constant not found: " + str);
    }

    @Override
    public JsonPrimitive toJson(T value) {
        return new JsonPrimitive(value.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public StreamCodec<? super FriendlyByteBuf, T> streamCodec() {
        return NeoForgeStreamCodecs.enumCodec(this.cls);
    }

    @Override
    public Optional<T> correct(JsonElement json, ConfigCorrection<T> correction) {
        if (json.isJsonPrimitive() || json.isJsonNull()) {
            String str = json.isJsonNull() ? "null" : json.getAsString().toLowerCase(Locale.ROOT).strip();
            T[] enums = this.cls.getEnumConstants();
            for (T e : enums) {
                if (e.name().toLowerCase(Locale.ROOT).equals(str) || str.startsWith(e.name()) || str.endsWith(e.name())) {
                    return Optional.of(e);
                }
            }
            try {
                int ordinal = Integer.parseInt(str);
                if (ordinal >= 0 && ordinal < enums.length) {
                    return Optional.of(enums[ordinal]);
                } else {
                    return Optional.empty();
                }
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<String> comment() {
        return List.of("Allowed values: " + Arrays.stream(this.cls.getEnumConstants())
                        .map(e -> e.name().toLowerCase(Locale.ROOT))
                        .collect(Collectors.joining(", "))
        );
    }

    @Override
    public ConfigEditor<T> createEditor(ValidatorInfo<?> validator) {
        return ConfigEditor.toggle(ImmutableList.copyOf(this.cls.getEnumConstants()), e -> Component.literal(e.name().toLowerCase(Locale.ROOT)));
    }
}

package io.github.noeppi_noeppi.libx.impl.config.mappers.special;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.github.noeppi_noeppi.libx.config.ValidatorInfo;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import io.github.noeppi_noeppi.libx.config.correct.ConfigCorrection;
import io.github.noeppi_noeppi.libx.config.gui.ConfigEditor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;
import java.util.stream.Collectors;

public class EnumValueMappers implements ValueMapper<Enum<?>, JsonPrimitive> {
    
    private static final Map<Class<? extends Enum<?>>, EnumValueMappers> mappers = new HashMap<>();
    
    public static EnumValueMappers getMapper(Class<? extends Enum<?>> enumClass) {
        if (!enumClass.isEnum()) {
            throw new IllegalArgumentException("Can't get enum serializer for non-enum class: " + enumClass);
        } else if (enumClass.getEnumConstants().length == 0) {
            throw new IllegalArgumentException("Can't get enum serializer for empty enum: " + enumClass);
        } else if (mappers.containsKey(enumClass)) {
            return mappers.get(enumClass);
        } else {
            EnumValueMappers mapper = new EnumValueMappers(enumClass);
            mappers.put(enumClass, mapper);
            return mapper;
        }
    }
    
    private final Class<? extends Enum<?>> clazz;

    public EnumValueMappers(Class<? extends Enum<?>> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Class<Enum<?>> type() {
        //noinspection unchecked
        return (Class<Enum<?>>) (Class<?>) Enum.class;
    }

    @Override
    public Class<JsonPrimitive> element() {
        return JsonPrimitive.class;
    }

    @Override
    public Enum<?> fromJson(JsonPrimitive json) {
        String str = json.getAsString().toLowerCase(Locale.ROOT).strip();
        Enum<?>[] enums = this.clazz.getEnumConstants();
        for (Enum<?> e : enums) {
            if (e.name().toLowerCase(Locale.ROOT).equals(str)) {
                return e;
            }
        }
        throw new NoSuchElementException("Enum constant not found: " + str);
    }

    @Override
    public JsonPrimitive toJson(Enum<?> value) {
        return new JsonPrimitive(value.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public Enum<?> fromNetwork(FriendlyByteBuf buffer) {
        return this.clazz.getEnumConstants()[buffer.readVarInt()];
    }

    @Override
    public void toNetwork(Enum<?> value, FriendlyByteBuf buffer) {
        buffer.writeVarInt(value.ordinal());
    }

    @Override
    public Optional<Enum<?>> correct(JsonElement json, ConfigCorrection<Enum<?>> correction) {
        if (json.isJsonPrimitive() && json.isJsonNull()) {
            String str = json.isJsonNull() ? "null" : json.getAsString().toLowerCase(Locale.ROOT).strip();
            Enum<?>[] enums = this.clazz.getEnumConstants();
            for (Enum<?> e : enums) {
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
        return List.of("Allowed values: " + Arrays.stream(this.clazz.getEnumConstants())
                        .map(e -> e.name().toLowerCase(Locale.ROOT))
                        .collect(Collectors.joining(", "))
        );
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ConfigEditor<Enum<?>> createEditor(ValidatorInfo<?> validator) {
        return ConfigEditor.unsupported(this.clazz.getEnumConstants()[0]);
    }
}

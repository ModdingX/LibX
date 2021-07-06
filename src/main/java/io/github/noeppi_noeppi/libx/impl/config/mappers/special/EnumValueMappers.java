package io.github.noeppi_noeppi.libx.impl.config.mappers.special;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonPrimitive;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import net.minecraft.network.PacketBuffer;

import java.util.*;
import java.util.stream.Collectors;

public class EnumValueMappers implements ValueMapper<Enum<?>, JsonPrimitive> {
    
    private static final Map<Class<? extends Enum<?>>, EnumValueMappers> mappers = new HashMap<>();
    
    public static EnumValueMappers getMapper(Class<? extends Enum<?>> enumClass) {
        if (!enumClass.isEnum()) {
            throw new IllegalArgumentException("Can't get enum serializer for non-enum class: " + enumClass);
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
    public Enum<?> fromJSON(JsonPrimitive json) {
        String str = json.getAsString();
        Enum<?>[] enums = this.clazz.getEnumConstants();
        for (Enum<?> e : enums) {
            if (e.name().equalsIgnoreCase(str)) {
                return e;
            }
        }
        throw new NoSuchElementException("Enum constant not found: " + str);
    }

    @Override
    public JsonPrimitive toJSON(Enum<?> value) {
        return new JsonPrimitive(value.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public Enum<?> read(PacketBuffer buffer) {
        return this.clazz.getEnumConstants()[buffer.readVarInt()];
    }

    @Override
    public void write(Enum<?> value, PacketBuffer buffer) {
        buffer.writeVarInt(value.ordinal());
    }

    @Override
    public List<String> comment() {
        return ImmutableList.of(
                "Allowed values: " + Arrays.stream(this.clazz.getEnumConstants())
                        .map(e -> e.name().toLowerCase(Locale.ROOT))
                        .collect(Collectors.joining(", "))
        );
    }
}

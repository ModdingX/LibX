package io.github.noeppi_noeppi.libx.impl.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonPrimitive;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

public class EnumConfigMapper implements ValueMapper<Enum<?>, JsonPrimitive> {

    public static final ResourceLocation ID = new ResourceLocation("minecraft", "enum");
    
    private static final Map<Class<? extends Enum<?>>, EnumConfigMapper> mappers = new HashMap<>();
    
    public static EnumConfigMapper getMapper(Class<? extends Enum<?>> enumClass) {
        if (!enumClass.isEnum()) {
            throw new IllegalArgumentException("Can't get enum serializer for non-enum class: " + enumClass);
        } else if (mappers.containsKey(enumClass)) {
            return mappers.get(enumClass);
        } else {
            EnumConfigMapper mapper = new EnumConfigMapper(enumClass);
            mappers.put(enumClass, mapper);
            return mapper;
        }
    }
    
    private final Class<? extends Enum<?>> clazz;

    public EnumConfigMapper(Class<? extends Enum<?>> clazz) {
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
    public Enum<?> fromJSON(JsonPrimitive json, Class<?> elementType) {
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
    public JsonPrimitive toJSON(Enum<?> value, Class<?> elementType) {
        return new JsonPrimitive(value.name().toLowerCase());
    }

    @Override
    public Enum<?> read(PacketBuffer buffer, Class<?> elementType) {
        return this.clazz.getEnumConstants()[buffer.readVarInt()];
    }

    @Override
    public void write(Enum<?> value, PacketBuffer buffer, Class<?> elementType) {
        buffer.writeVarInt(value.ordinal());
    }

    @Override
    public List<String> comment(Class<?> elementType) {
        return ImmutableList.of(
                "Allowed values: " + Arrays.stream(this.clazz.getEnumConstants())
                        .map(e -> e.name().toLowerCase())
                        .collect(Collectors.joining(", "))
        );
    }
}

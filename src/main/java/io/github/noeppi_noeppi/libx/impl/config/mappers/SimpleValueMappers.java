package io.github.noeppi_noeppi.libx.impl.config.mappers;

import com.google.gson.JsonPrimitive;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SimpleValueMappers {
    
    public static final ValueMapper<Boolean, JsonPrimitive> BOOLEAN = createPrimitive(Boolean.class, JsonPrimitive::getAsBoolean, JsonPrimitive::new, FriendlyByteBuf::readBoolean, FriendlyByteBuf::writeBoolean);
    public static final ValueMapper<Byte, JsonPrimitive> BYTE = createPrimitive(Byte.class, JsonPrimitive::getAsByte, JsonPrimitive::new, FriendlyByteBuf::readByte, (BiConsumer<FriendlyByteBuf, Byte>) FriendlyByteBuf::writeByte);
    public static final ValueMapper<Short, JsonPrimitive> SHORT = createPrimitive(Short.class, JsonPrimitive::getAsShort, JsonPrimitive::new, FriendlyByteBuf::readShort, (BiConsumer<FriendlyByteBuf, Short>) FriendlyByteBuf::writeShort);
    public static final ValueMapper<Integer, JsonPrimitive> INTEGER = createPrimitive(Integer.class, JsonPrimitive::getAsInt, JsonPrimitive::new, FriendlyByteBuf::readVarInt, FriendlyByteBuf::writeVarInt);
    public static final ValueMapper<Long, JsonPrimitive> LONG = createPrimitive(Long.class, JsonPrimitive::getAsLong, JsonPrimitive::new, FriendlyByteBuf::readVarLong, FriendlyByteBuf::writeVarLong);
    public static final ValueMapper<Float, JsonPrimitive> FLOAT = createPrimitive(Float.class, JsonPrimitive::getAsFloat, JsonPrimitive::new, FriendlyByteBuf::readFloat, FriendlyByteBuf::writeFloat);
    public static final ValueMapper<Double, JsonPrimitive> DOUBLE = createPrimitive(Double.class, JsonPrimitive::getAsDouble, JsonPrimitive::new, FriendlyByteBuf::readDouble, FriendlyByteBuf::writeDouble);
    public static final ValueMapper<String, JsonPrimitive> STRING = createPrimitive(String.class, JsonPrimitive::getAsString, JsonPrimitive::new, buffer -> buffer.readUtf(0x7fff), (buffer, string) -> buffer.writeUtf(string, 0x7fff));
    
    private static <T> ValueMapper<T, JsonPrimitive> createPrimitive(Class<T> typeClass, Function<JsonPrimitive, T> fromJSON, Function<T, JsonPrimitive> toJSON, Function<FriendlyByteBuf, T> read, BiConsumer<FriendlyByteBuf, T> write) {
        
        return new ValueMapper<T, JsonPrimitive>() {
            
            @Override
            public Class<T> type() {
                return typeClass;
            }

            @Override
            public Class<JsonPrimitive> element() {
                return JsonPrimitive.class;
            }

            @Override
            public T fromJSON(JsonPrimitive json) {
                return fromJSON.apply(json);
            }

            @Override
            public JsonPrimitive toJSON(T value) {
                return toJSON.apply(value);
            }

            @Override
            public T read(FriendlyByteBuf buffer) {
                return read.apply(buffer);
            }

            @Override
            public void write(T value, FriendlyByteBuf buffer) {
                write.accept(buffer, value);
            }
        };
    }
}

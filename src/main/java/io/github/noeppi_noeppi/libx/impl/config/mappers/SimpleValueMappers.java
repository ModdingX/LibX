package io.github.noeppi_noeppi.libx.impl.config.mappers;

import com.google.gson.JsonPrimitive;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import net.minecraft.network.PacketBuffer;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SimpleValueMappers {
    
    public static final ValueMapper<Boolean, JsonPrimitive> BOOLEAN = createPrimitive(Boolean.class, JsonPrimitive::getAsBoolean, JsonPrimitive::new, PacketBuffer::readBoolean, PacketBuffer::writeBoolean);
    public static final ValueMapper<Byte, JsonPrimitive> BYTE = createPrimitive(Byte.class, JsonPrimitive::getAsByte, JsonPrimitive::new, PacketBuffer::readByte, (BiConsumer<PacketBuffer, Byte>) PacketBuffer::writeByte);
    public static final ValueMapper<Short, JsonPrimitive> SHORT = createPrimitive(Short.class, JsonPrimitive::getAsShort, JsonPrimitive::new, PacketBuffer::readShort, (BiConsumer<PacketBuffer, Short>) PacketBuffer::writeShort);
    public static final ValueMapper<Integer, JsonPrimitive> INTEGER = createPrimitive(Integer.class, JsonPrimitive::getAsInt, JsonPrimitive::new, PacketBuffer::readVarInt, PacketBuffer::writeVarInt);
    public static final ValueMapper<Long, JsonPrimitive> LONG = createPrimitive(Long.class, JsonPrimitive::getAsLong, JsonPrimitive::new, PacketBuffer::readVarLong, PacketBuffer::writeVarLong);
    public static final ValueMapper<Float, JsonPrimitive> FLOAT = createPrimitive(Float.class, JsonPrimitive::getAsFloat, JsonPrimitive::new, PacketBuffer::readFloat, PacketBuffer::writeFloat);
    public static final ValueMapper<Double, JsonPrimitive> DOUBLE = createPrimitive(Double.class, JsonPrimitive::getAsDouble, JsonPrimitive::new, PacketBuffer::readDouble, PacketBuffer::writeDouble);
    public static final ValueMapper<String, JsonPrimitive> STRING = createPrimitive(String.class, JsonPrimitive::getAsString, JsonPrimitive::new, buffer -> buffer.readString(0x7fff), (buffer, string) -> buffer.writeString(string, 0x7fff));
    
    private static <T> ValueMapper<T, JsonPrimitive> createPrimitive(Class<T> typeClass, Function<JsonPrimitive, T> fromJSON, Function<T, JsonPrimitive> toJSON, Function<PacketBuffer, T> read, BiConsumer<PacketBuffer, T> write) {
        
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
            public T read(PacketBuffer buffer) {
                return read.apply(buffer);
            }

            @Override
            public void write(T value, PacketBuffer buffer) {
                write.accept(buffer, value);
            }
        };
    }
}

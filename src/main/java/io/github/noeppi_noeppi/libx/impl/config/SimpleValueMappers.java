package io.github.noeppi_noeppi.libx.impl.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import io.github.noeppi_noeppi.libx.config.ConfigManager;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import net.minecraft.network.PacketBuffer;

import java.util.List;
import java.util.Map;
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
    
    public static final ValueMapper<List<?>, JsonArray> LIST = new ValueMapper<List<?>, JsonArray>() {

        @Override
        public Class<List<?>> type() {
            //noinspection unchecked
            return (Class<List<?>>) (Class<?>) List.class;
        }

        @Override
        public Class<JsonArray> element() {
            return JsonArray.class;
        }

        @Override
        public List<?> fromJSON(JsonArray json, Class<?> elementType) {
            //noinspection unchecked
            ValueMapper<Object, JsonElement> mapper = (ValueMapper<Object, JsonElement>) ConfigManager.getMapper(null, elementType);
            ImmutableList.Builder<Object> builder = ImmutableList.builder();
            for (int i = 0; i < json.size(); i++) {
                JsonElement element = json.get(i);
                if (mapper.element().isAssignableFrom(element.getClass())) {
                    builder.add(mapper.fromJSON(element, void.class));
                } else {
                    throw new JsonSyntaxException("Can't deserialise object of type " + mapper.type() + " from json of type " + element.getClass().getSimpleName());
                }
            }
            return builder.build();
        }

        @Override
        public JsonArray toJSON(List<?> value, Class<?> elementType) {
            //noinspection unchecked
            ValueMapper<Object, JsonElement> mapper = (ValueMapper<Object, JsonElement>) ConfigManager.getMapper(null, elementType);
            JsonArray array = new JsonArray();
            for (Object element : value) {
                array.add(mapper.toJSON(element, void.class));
            }
            return array;
        }

        @Override
        public List<?> read(PacketBuffer buffer, Class<?> elementType) {
            //noinspection unchecked
            ValueMapper<Object, JsonElement> mapper = (ValueMapper<Object, JsonElement>) ConfigManager.getMapper(null, elementType);
            int size = buffer.readVarInt();
            ImmutableList.Builder<Object> builder = ImmutableList.builder();
            for (int i = 0; i < size; i++) {
                builder.add(mapper.read(buffer, void.class));
            }
            return builder.build();
        }

        @Override
        public void write(List<?> value, PacketBuffer buffer, Class<?> elementType) {
            //noinspection unchecked
            ValueMapper<Object, JsonElement> mapper = (ValueMapper<Object, JsonElement>) ConfigManager.getMapper(null, elementType);
            buffer.writeVarInt(value.size());
            for (Object elem : value) {
                mapper.write(elem, buffer, void.class);
            }
        }
    };

    public static final ValueMapper<Map<String, ?>, JsonObject> MAP = new ValueMapper<Map<String, ?>, JsonObject>() {

        @Override
        public Class<Map<String, ?>> type() {
            //noinspection unchecked
            return (Class<Map<String, ?>>) (Class<?>) Map.class;
        }

        @Override
        public Class<JsonObject> element() {
            return JsonObject.class;
        }

        @Override
        public Map<String, ?> fromJSON(JsonObject json, Class<?> elementType) {
            //noinspection unchecked
            ValueMapper<Object, JsonElement> mapper = (ValueMapper<Object, JsonElement>) ConfigManager.getMapper(null, elementType);
            ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                if (mapper.element().isAssignableFrom(entry.getValue().getClass())) {
                    builder.put(entry.getKey(), mapper.fromJSON(entry.getValue(), void.class));
                } else {
                    throw new JsonSyntaxException("Can't deserialise object of type " + mapper.type() + " from json of type " + entry.getValue().getClass().getSimpleName());
                }
            }
            return builder.build();
        }

        @Override
        public JsonObject toJSON(Map<String, ?> value, Class<?> elementType) {
            //noinspection unchecked
            ValueMapper<Object, JsonElement> mapper = (ValueMapper<Object, JsonElement>) ConfigManager.getMapper(null, elementType);
            JsonObject object = new JsonObject();
            for (Map.Entry<String, ?> entry : value.entrySet()) {
                object.add(entry.getKey(), mapper.toJSON(entry.getValue(), void.class));
            }
            return object;
        }

        @Override
        public Map<String, ?> read(PacketBuffer buffer, Class<?> elementType) {
            //noinspection unchecked
            ValueMapper<Object, JsonElement> mapper = (ValueMapper<Object, JsonElement>) ConfigManager.getMapper(null, elementType);
            int size = buffer.readVarInt();
            ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
            for (int i = 0; i < size; i++) {
                builder.put(buffer.readString(0x7fff), mapper.read(buffer, void.class));
            }
            return builder.build();
        }

        @Override
        public void write(Map<String, ?> value, PacketBuffer buffer, Class<?> elementType) {
            //noinspection unchecked
            ValueMapper<Object, JsonElement> mapper = (ValueMapper<Object, JsonElement>) ConfigManager.getMapper(null, elementType);
            buffer.writeVarInt(value.size());
            for (Map.Entry<String, ?> entry : value.entrySet()) {
                buffer.writeString(entry.getKey(), 0x7fff);
                mapper.write(entry.getValue(), buffer, void.class);
            }
        }
    };
    
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
            public T fromJSON(JsonPrimitive json, Class<?> elementType) {
                return fromJSON.apply(json);
            }

            @Override
            public JsonPrimitive toJSON(T value, Class<?> elementType) {
                return toJSON.apply(value);
            }

            @Override
            public T read(PacketBuffer buffer, Class<?> elementType) {
                return read.apply(buffer);
            }

            @Override
            public void write(T value, PacketBuffer buffer, Class<?> elementType) {
                write.accept(buffer, value);
            }
        };
    }
}

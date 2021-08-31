package io.github.noeppi_noeppi.libx.impl.config.mappers;

import com.google.gson.JsonPrimitive;
import io.github.noeppi_noeppi.libx.config.ValidatorInfo;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import io.github.noeppi_noeppi.libx.config.gui.ConfigEditor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SimpleValueMappers {
    
    public static final ValueMapper<Boolean, JsonPrimitive> BOOLEAN = createPrimitive(Boolean.class, JsonPrimitive::getAsBoolean, JsonPrimitive::new, FriendlyByteBuf::readBoolean, FriendlyByteBuf::writeBoolean, false);
    public static final ValueMapper<Byte, JsonPrimitive> BYTE = createPrimitive(Byte.class, JsonPrimitive::getAsByte, JsonPrimitive::new, FriendlyByteBuf::readByte, (BiConsumer<FriendlyByteBuf, Byte>) FriendlyByteBuf::writeByte, (byte) 0);
    public static final ValueMapper<Short, JsonPrimitive> SHORT = createPrimitive(Short.class, JsonPrimitive::getAsShort, JsonPrimitive::new, FriendlyByteBuf::readShort, (BiConsumer<FriendlyByteBuf, Short>) FriendlyByteBuf::writeShort, (short) 0);
    public static final ValueMapper<Integer, JsonPrimitive> INTEGER = createPrimitive(Integer.class, JsonPrimitive::getAsInt, JsonPrimitive::new, FriendlyByteBuf::readVarInt, FriendlyByteBuf::writeVarInt, 0);
    public static final ValueMapper<Long, JsonPrimitive> LONG = createPrimitive(Long.class, JsonPrimitive::getAsLong, JsonPrimitive::new, FriendlyByteBuf::readVarLong, FriendlyByteBuf::writeVarLong, (long) 0);
    public static final ValueMapper<Float, JsonPrimitive> FLOAT = createPrimitive(Float.class, JsonPrimitive::getAsFloat, JsonPrimitive::new, FriendlyByteBuf::readFloat, FriendlyByteBuf::writeFloat, 0f);
    public static final ValueMapper<Double, JsonPrimitive> DOUBLE = createPrimitive(Double.class, JsonPrimitive::getAsDouble, JsonPrimitive::new, FriendlyByteBuf::readDouble, FriendlyByteBuf::writeDouble, 0d);
    public static final ValueMapper<String, JsonPrimitive> STRING = createPrimitive(String.class, JsonPrimitive::getAsString, JsonPrimitive::new, buffer -> buffer.readUtf(0x7fff), (buffer, string) -> buffer.writeUtf(string, 0x7fff), "");
    
    private static <T> ValueMapper<T, JsonPrimitive> createPrimitive(Class<T> typeClass, Function<JsonPrimitive, T> fromJSON, Function<T, JsonPrimitive> toJSON, Function<FriendlyByteBuf, T> read, BiConsumer<FriendlyByteBuf, T> write, T defaultValue) {
        
        return new ValueMapper<>() {

            @Override
            public Class<T> type() {
                return typeClass;
            }

            @Override
            public Class<JsonPrimitive> element() {
                return JsonPrimitive.class;
            }

            @Override
            public T fromJson(JsonPrimitive json) {
                return fromJSON.apply(json);
            }

            @Override
            public JsonPrimitive toJson(T value) {
                return toJSON.apply(value);
            }

            @Override
            public T fromNetwork(FriendlyByteBuf buffer) {
                return read.apply(buffer);
            }

            @Override
            public void toNetwork(T value, FriendlyByteBuf buffer) {
                write.accept(buffer, value);
            }

            @Override
            @OnlyIn(Dist.CLIENT)
            public ConfigEditor<T> createEditor(ValidatorInfo<?> validator) {
                return ConfigEditor.unsupported(defaultValue);
            }
        };
    }
}

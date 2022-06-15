package org.moddingx.libx.impl.config.mappers;

import com.google.gson.JsonPrimitive;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.InputProperties;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validate.*;
import org.moddingx.libx.config.validator.ValidatorInfo;
import org.moddingx.libx.impl.config.gui.editor.CheckEditor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;

public class SimpleValueMappers {
    
    public static final ValueMapper<Boolean, JsonPrimitive> BOOLEAN = new ValueMapper<>() {

        @Override
        public Class<Boolean> type() {
            return Boolean.class;
        }

        @Override
        public Class<JsonPrimitive> element() {
            return JsonPrimitive.class;
        }

        @Override
        public Boolean fromJson(JsonPrimitive json) {
            return json.getAsBoolean();
        }

        @Override
        public JsonPrimitive toJson(Boolean value) {
            return new JsonPrimitive(value);
        }

        @Override
        public Boolean fromNetwork(FriendlyByteBuf buffer) {
            return buffer.readBoolean();
        }

        @Override
        public void toNetwork(Boolean value, FriendlyByteBuf buffer) {
            buffer.writeBoolean(value);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public ConfigEditor<Boolean> createEditor(ValidatorInfo<?> validator) {
            return CheckEditor.INSTANCE;
        }
    };

    public static final ValueMapper<Byte, JsonPrimitive> BYTE = new ValueMapper<>() {

        @Override
        public Class<Byte> type() {
            return Byte.class;
        }

        @Override
        public Class<JsonPrimitive> element() {
            return JsonPrimitive.class;
        }

        @Override
        public Byte fromJson(JsonPrimitive json) {
            return json.getAsByte();
        }

        @Override
        public JsonPrimitive toJson(Byte value) {
            return new JsonPrimitive(value);
        }

        @Override
        public Byte fromNetwork(FriendlyByteBuf buffer) {
            return buffer.readByte();
        }

        @Override
        public void toNetwork(Byte value, FriendlyByteBuf buffer) {
            buffer.writeByte(value);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public ConfigEditor<Byte> createEditor(ValidatorInfo<?> validator) {
            return ConfigEditor.input(number(false, (byte) 0, Byte::parseByte), validator);
        }
    };

    public static final ValueMapper<Short, JsonPrimitive> SHORT = new ValueMapper<>() {

        @Override
        public Class<Short> type() {
            return Short.class;
        }

        @Override
        public Class<JsonPrimitive> element() {
            return JsonPrimitive.class;
        }

        @Override
        public Short fromJson(JsonPrimitive json) {
            return json.getAsShort();
        }

        @Override
        public JsonPrimitive toJson(Short value) {
            return new JsonPrimitive(value);
        }

        @Override
        public Short fromNetwork(FriendlyByteBuf buffer) {
            return buffer.readShort();
        }

        @Override
        public void toNetwork(Short value, FriendlyByteBuf buffer) {
            buffer.writeShort(value);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public ConfigEditor<Short> createEditor(ValidatorInfo<?> validator) {
            ShortRange range = validator.value(ShortRange.class);
            if (range != null && range.min() != Short.MIN_VALUE && range.max() != Short.MAX_VALUE) {
                return ConfigEditor.slider(
                        value -> (value - range.min()) / ((double) (range.max() - range.min())),
                        value -> (short) Math.round(Mth.lerp(value, range.min(), range.max()))
                );
            } else {
                return ConfigEditor.input(number(false, (short) 0, Short::parseShort), validator);
            }
        }
    };

    public static final ValueMapper<Integer, JsonPrimitive> INTEGER = new ValueMapper<>() {

        @Override
        public Class<Integer> type() {
            return Integer.class;
        }

        @Override
        public Class<JsonPrimitive> element() {
            return JsonPrimitive.class;
        }

        @Override
        public Integer fromJson(JsonPrimitive json) {
            return json.getAsInt();
        }

        @Override
        public JsonPrimitive toJson(Integer value) {
            return new JsonPrimitive(value);
        }

        @Override
        public Integer fromNetwork(FriendlyByteBuf buffer) {
            return buffer.readVarInt();
        }

        @Override
        public void toNetwork(Integer value, FriendlyByteBuf buffer) {
            buffer.writeVarInt(value);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public ConfigEditor<Integer> createEditor(ValidatorInfo<?> validator) {
            IntRange range = validator.value(IntRange.class);
            if (range != null && range.min() != Integer.MIN_VALUE && range.max() != Integer.MAX_VALUE) {
                return ConfigEditor.slider(
                        value -> (value - range.min()) / ((double) (range.max() - range.min())),
                        value -> (int) Math.round(Mth.lerp(value, range.min(), range.max()))
                );
            } else {
                return ConfigEditor.input(number(false, 0, Integer::parseInt), validator);
            }
        }
    };

    public static final ValueMapper<Long, JsonPrimitive> LONG = new ValueMapper<>() {

        @Override
        public Class<Long> type() {
            return Long.class;
        }

        @Override
        public Class<JsonPrimitive> element() {
            return JsonPrimitive.class;
        }

        @Override
        public Long fromJson(JsonPrimitive json) {
            return json.getAsLong();
        }

        @Override
        public JsonPrimitive toJson(Long value) {
            return new JsonPrimitive(value);
        }

        @Override
        public Long fromNetwork(FriendlyByteBuf buffer) {
            return buffer.readVarLong();
        }

        @Override
        public void toNetwork(Long value, FriendlyByteBuf buffer) {
            buffer.writeVarLong(value);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public ConfigEditor<Long> createEditor(ValidatorInfo<?> validator) {
            LongRange range = validator.value(LongRange.class);
            if (range != null && range.min() != Long.MIN_VALUE && range.max() != Long.MAX_VALUE) {
                return ConfigEditor.slider(
                        value -> (value - range.min()) / ((double) (range.max() - range.min())),
                        value -> Math.round(Mth.lerp(value, range.min(), range.max()))
                );
            } else {
                return ConfigEditor.input(number(false, (long) 0, Long::parseLong), validator);
            }
        }
    };

    public static final ValueMapper<Float, JsonPrimitive> FLOAT = new ValueMapper<>() {

        @Override
        public Class<Float> type() {
            return Float.class;
        }

        @Override
        public Class<JsonPrimitive> element() {
            return JsonPrimitive.class;
        }

        @Override
        public Float fromJson(JsonPrimitive json) {
            return json.getAsFloat();
        }

        @Override
        public JsonPrimitive toJson(Float value) {
            return new JsonPrimitive(value);
        }

        @Override
        public Float fromNetwork(FriendlyByteBuf buffer) {
            return buffer.readFloat();
        }

        @Override
        public void toNetwork(Float value, FriendlyByteBuf buffer) {
            buffer.writeFloat(value);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public ConfigEditor<Float> createEditor(ValidatorInfo<?> validator) {
            FloatRange range = validator.value(FloatRange.class);
            if (range != null && Float.isFinite(range.min()) && Float.isFinite(range.max())) {
                return ConfigEditor.slider(
                        value -> (value - range.min()) / ((double) (range.max() - range.min())),
                        value -> BigDecimal.valueOf(Mth.lerp(value, range.min(), range.max())).setScale(3, RoundingMode.HALF_UP).floatValue()
                );
            } else {
                return ConfigEditor.input(number(true, 0f, Float::parseFloat), validator);
            }
        }
    };

    public static final ValueMapper<Double, JsonPrimitive> DOUBLE = new ValueMapper<>() {

        @Override
        public Class<Double> type() {
            return Double.class;
        }

        @Override
        public Class<JsonPrimitive> element() {
            return JsonPrimitive.class;
        }

        @Override
        public Double fromJson(JsonPrimitive json) {
            return json.getAsDouble();
        }

        @Override
        public JsonPrimitive toJson(Double value) {
            return new JsonPrimitive(value);
        }

        @Override
        public Double fromNetwork(FriendlyByteBuf buffer) {
            return buffer.readDouble();
        }

        @Override
        public void toNetwork(Double value, FriendlyByteBuf buffer) {
            buffer.writeDouble(value);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public ConfigEditor<Double> createEditor(ValidatorInfo<?> validator) {
            DoubleRange range = validator.value(DoubleRange.class);
            if (range != null && Double.isFinite(range.min()) && Double.isFinite(range.max())) {
                return ConfigEditor.slider(
                        value -> (value - range.min()) / (range.max() - range.min()),
                        value -> BigDecimal.valueOf(Mth.lerp(value, range.min(), range.max())).setScale(3, RoundingMode.HALF_UP).doubleValue()
                );
            } else {
                return ConfigEditor.input(number(true, 0d, Double::parseDouble), validator);
            }
        }
    };

    public static final ValueMapper<String, JsonPrimitive> STRING = new ValueMapper<>() {

        @Override
        public Class<String> type() {
            return String.class;
        }

        @Override
        public Class<JsonPrimitive> element() {
            return JsonPrimitive.class;
        }

        @Override
        public String fromJson(JsonPrimitive json) {
            return json.getAsString();
        }

        @Override
        public JsonPrimitive toJson(String value) {
            return new JsonPrimitive(value);
        }

        @Override
        public String fromNetwork(FriendlyByteBuf buffer) {
            return buffer.readUtf();
        }

        @Override
        public void toNetwork(String value, FriendlyByteBuf buffer) {
            buffer.writeUtf(value);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public ConfigEditor<String> createEditor(ValidatorInfo<?> validator) {
            return ConfigEditor.input(validator);
        }
    };
    
    private static <T> InputProperties<T> number(boolean floating, T defaultValue, Function<String, T> parse) {
        return new InputProperties<>() {

            @Override
            public T defaultValue() {
                return defaultValue;
            }

            @Override
            public String toString(T t) {
                if (floating) {
                    return t.toString().replace('E', 'e');
                } else {
                    return t.toString();
                }
            }

            @Override
            public T valueOf(String str) {
                return parse.apply(str);
            }

            @Override
            public boolean canInputChar(char chr) {
                if (floating) {
                    return (chr >= '0' && chr <= '9') || chr == '-' || chr == 'e' || chr == 'E' || chr == '.';
                } else {
                    return (chr >= '0' && chr <= '9') || chr == '-';
                }
            }

            @Override
            public boolean isValid(String str) {
                try {
                    parse.apply(str);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        };
    }
}

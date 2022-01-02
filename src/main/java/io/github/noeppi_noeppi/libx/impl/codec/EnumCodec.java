package io.github.noeppi_noeppi.libx.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.github.noeppi_noeppi.libx.codec.CodecHelper;
import net.minecraftforge.common.IExtensibleEnum;

import java.util.HashMap;
import java.util.Map;

public class EnumCodec<A extends Enum<A>> implements Codec<A> {

    private static final Map<Class<? extends Enum<?>>, EnumCodec<?>> INSTANCES = new HashMap<>();

    public static synchronized <A extends Enum<A>> EnumCodec<A> get(Class<A> clazz) {
        //noinspection unchecked
        return (EnumCodec<A>) INSTANCES.computeIfAbsent(clazz, c -> new EnumCodec<>(clazz));
    }

    private final Class<A> clazz;
    private final boolean extensible;

    private EnumCodec(Class<A> clazz) {
        if (!clazz.isEnum()) throw new IllegalArgumentException("Can't create enum codec for non-enum class: " + clazz);
        this.clazz = clazz;
        this.extensible = IExtensibleEnum.class.isAssignableFrom(this.clazz);
    }

    @Override
    public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
        if (ops.compressMaps() && !this.extensible) {
            return ops.mergeToPrimitive(prefix, ops.createInt(input.ordinal()));
        } else {
            return ops.mergeToPrimitive(prefix, ops.createString(input.name()));
        }
    }

    @Override
    public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
        if (ops.compressMaps() && !this.extensible) {
            return ops.getNumberValue(input)
                    .flatMap(number -> CodecHelper.doesNotThrow(() -> {
                        A[] elems = this.clazz.getEnumConstants();
                        try {
                            return elems[number.intValue()];
                        } catch (ArrayIndexOutOfBoundsException e) {
                            throw new IllegalStateException("Invalid enum constant id: " + number + " (" + number.intValue() + ")");
                        }
                    })).map(r -> Pair.of(r, ops.empty()));
        } else {
            return ops.getStringValue(input)
                    .flatMap(str -> CodecHelper.doesNotThrow(() -> Enum.valueOf(this.clazz, str)))
                    .map(r -> Pair.of(r, ops.empty()));
        }
    }
}

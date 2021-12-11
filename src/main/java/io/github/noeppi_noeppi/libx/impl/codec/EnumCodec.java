package io.github.noeppi_noeppi.libx.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.github.noeppi_noeppi.libx.codec.CodecHelper;

import java.util.HashMap;
import java.util.Map;

public class EnumCodec<A extends Enum<A>> implements Codec<A> {

    private static final Map<Class<? extends Enum<?>>, EnumCodec<?>> INSTANCES = new HashMap<>();

    public static synchronized <A extends Enum<A>> EnumCodec<A> get(Class<A> clazz) {
        //noinspection unchecked
        return (EnumCodec<A>) INSTANCES.computeIfAbsent(clazz, EnumCodec::new);
    }

    private final Class<A> clazz;

    private EnumCodec(Class<A> clazz) {
        this.clazz = clazz;
    }

    @Override
    public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
        return ops.mergeToPrimitive(prefix, ops.createString(input.name()));
    }

    @Override
    public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
        return ops.getStringValue(input)
                .flatMap(str -> CodecHelper.doesNotThrow(() -> Enum.valueOf(this.clazz, str)))
                .map(r -> Pair.of(r, ops.empty()));
    }
}

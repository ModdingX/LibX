package org.moddingx.libx.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraftforge.common.IExtensibleEnum;
import org.moddingx.libx.codec.CodecHelper;

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
            return CodecHelper.or(
                    () -> this.decodeById(ops, input),
                    () -> this.decodeByName(ops, input)
            );
        } else {
            return CodecHelper.or(
                    () -> this.decodeByName(ops, input),
                    () -> this.decodeById(ops, input)
            );
        }
    }
    
    private <T> DataResult<Pair<A, T>> decodeById(DynamicOps<T> ops, T input) {
        return ops.getNumberValue(input)
                .flatMap(number -> CodecHelper.doesNotThrow(() -> {
                    A[] elems = this.clazz.getEnumConstants();
                    try {
                        return elems[number.intValue()];
                    } catch (ArrayIndexOutOfBoundsException e) {
                        throw new IllegalStateException("Invalid enum constant id: " + number + " (" + number.intValue() + ")");
                    }
                })).map(r -> Pair.of(r, ops.empty()));
    }
    
    private <T> DataResult<Pair<A, T>> decodeByName(DynamicOps<T> ops, T input) {
        return ops.getStringValue(input)
                    .flatMap(str -> CodecHelper.doesNotThrow(() -> Enum.valueOf(this.clazz, str)))
                    .map(r -> Pair.of(r, ops.empty()));
    }

    @Override
    public String toString() {
        return "EnumCodec[" + this.clazz.getName() + "]";
    }
}

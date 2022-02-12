package io.github.noeppi_noeppi.libx.codec;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * An encoder that can encode and decode elements of a specific type.
 * This is used to create type mapped codecs that behave differently depending on the
 * {@link DynamicOps} they are used on.
 * A {@link TypedEncoder} is assumed to be able to encode and decode with every dynamic
 * ops whose {@link DynamicOps#empty() empty} element is a subclass of the class returned
 * by {@link #resultClass()}.
 */
public interface TypedEncoder<T, R> {

    /**
     * Gets the class of elements this {@link TypedEncoder} can encode to.
     */
    Class<R> resultClass();

    /**
     * Encodes a value.
     */
    DataResult<R> encode(T value);
    
    /**
     * Decodes a value.
     */
    DataResult<T> decode(R value);

    /**
     * Casts this {@link TypedEncoder} given some {@link DynamicOps}. Returns {@code null} if
     * the {@link TypedEncoder} can't be used on the given {@link DynamicOps}.
     * This can be overridden to better which {@link DynamicOps} an encoder accepts.
     */
    @Nullable
    default <N> TypedEncoder<T, N> cast(DynamicOps<N> ops) {
        if (this.resultClass().isAssignableFrom(ops.empty().getClass())) {
            //noinspection unchecked
            return (TypedEncoder<T, N>) this;
        } else {
            return null;
        }
    }

    /**
     * Creates a new {@link TypedEncoder} given two functions to encode and decode an object.
     * If a functions throws an exception, it is wrapped into an errored {@link DataResult}.
     */
    static <T, R> TypedEncoder<T, R> of(Class<R> resultClass, Function<? super T, ? extends R> encoder, Function<? super R, ? extends T> decoder) {
        return new TypedEncoder<>() {

            @Override
            public Class<R> resultClass() {
                return resultClass;
            }

            @Override
            public DataResult<R> encode(T value) {
                return CodecHelper.doesNotThrow(() -> encoder.apply(value));
            }

            @Override
            public DataResult<T> decode(R value) {
                return CodecHelper.doesNotThrow(() -> decoder.apply(value));
            }
        };
    }
}

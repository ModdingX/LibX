package org.moddingx.libx.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import org.moddingx.libx.codec.TypedEncoder;

import javax.annotation.Nullable;
import java.util.List;

public class TypeMappedCodec<A> implements Codec<A> {
    
    private final List<TypedEncoder<A, ?>> encoders;
    
    @Nullable
    private final Codec<A> fallback;

    public TypeMappedCodec(List<TypedEncoder<A, ?>> encoders, @Nullable Codec<A> fallback) {
        this.encoders = List.copyOf(encoders);
        this.fallback = fallback;
    }

    @Override
    public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
        for (TypedEncoder<A, ?> entry : this.encoders) {
            TypedEncoder<A, T> encoder = entry.cast(ops);
            if (encoder != null) {
                return encoder.encode(input).flatMap(v -> ops.mergeToPrimitive(prefix, v));
            }
        }
        if (this.fallback == null) {
            return DataResult.error("No fallback in type mapped codec: Can't encode to elements of type " + ops.empty().getClass());
        } else {
            return this.fallback.encode(input, ops, prefix);
        }
    }

    @Override
    public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
        for (TypedEncoder<A, ?> entry : this.encoders) {
            TypedEncoder<A, T> encoder = entry.cast(ops);
            if (encoder != null) {
                return encoder.decode(input).map(r -> Pair.of(r, ops.empty()));
            }
        }
        if (this.fallback == null) {
            return DataResult.error("No fallback in type mapped codec: Can't decode elements of type " + input.getClass());
        } else {
            return this.fallback.decode(ops, input);
        }
    }
}

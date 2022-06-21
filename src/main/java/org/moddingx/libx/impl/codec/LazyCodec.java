package org.moddingx.libx.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import org.moddingx.libx.util.lazy.LazyValue;

import java.util.function.Supplier;

public class LazyCodec<A> implements Codec<A> {

    private final LazyValue<Codec<A>> codec;

    public LazyCodec(Supplier<Codec<A>> codec) {
        this.codec = new LazyValue<>(codec);
    }

    @Override
    public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
        return this.codec.get().encode(input, ops, prefix);
    }

    @Override
    public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
        return this.codec.get().decode(ops, input);
    }
}

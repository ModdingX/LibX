package org.moddingx.libx.impl.codec;

import com.mojang.serialization.*;

import java.util.Optional;
import java.util.stream.Stream;

public class TrueOptionalMapCodec<A> extends MapCodec<Optional<A>> {

    private final Codec<A> codec;
    private final String name;

    public TrueOptionalMapCodec(Codec<A> codec, String name) {
        this.name = name;
        this.codec = codec;
    }

    @Override
    public <T> Stream<T> keys(final DynamicOps<T> ops) {
        return Stream.of(ops.createString(this.name));
    }

    @Override
    public <T> DataResult<Optional<A>> decode(final DynamicOps<T> ops, final MapLike<T> input) {
        final T value = input.get(this.name);
        if (value == null) {
            return DataResult.success(Optional.empty());
        } else {
            return this.codec.parse(ops, value).map(Optional::of);
        }
    }

    @Override
    public <T> RecordBuilder<T> encode(final Optional<A> input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
        if (input.isPresent()) {
            return prefix.add(this.name, this.codec.encodeStart(ops, input.get()));
        } else {
            return prefix;
        }
    }
}

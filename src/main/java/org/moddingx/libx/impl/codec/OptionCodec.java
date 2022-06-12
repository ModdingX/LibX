package org.moddingx.libx.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class OptionCodec<A> implements Codec<Optional<A>> {

    private final Codec<A> codec;

    public OptionCodec(Codec<A> codec) {
        this.codec = codec;
    }

    @Override
    public <T> DataResult<T> encode(Optional<A> input, DynamicOps<T> ops, T prefix) {
        if (!Objects.equals(prefix, ops.empty())) {
            return DataResult.error("Can't merge option to " + prefix + " of type " + prefix.getClass());
        } else if (input.isPresent()) {
            DataResult<T> result = this.codec.encode(input.get(), ops, ops.empty());
            return result.map(elem -> ops.createList(Stream.of(elem)));
        } else {
            return DataResult.success(ops.createList(Stream.empty()));
        }
    }

    @Override
    public <T> DataResult<Pair<Optional<A>, T>> decode(DynamicOps<T> ops, T input) {
        return ops.getStream(input).map(Stream::toList).flatMap(list -> {
            if (list.isEmpty()) {
                return DataResult.success(Pair.of(Optional.empty(), ops.empty()));
            } else if (list.size() == 1) {
                return this.codec.decode(ops, list.get(0)).flatMap(pair -> {
                    if (Objects.equals(pair.getSecond(), ops.empty())) {
                        return DataResult.success(Pair.of(Optional.of(pair.getFirst()), ops.empty()));
                    } else {
                        return DataResult.error("Can't decode option: child codec left over some data");
                    }
                });
            } else {
                return DataResult.error("Can't decode option: Expected a stream with exactly 0 or 1 elements, got " + list.size());
            }
        });
    }
}

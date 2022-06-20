package org.moddingx.libx.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;

import java.util.function.BiFunction;
import java.util.function.Function;

public class MapDispatchedCodec<A, K, V> implements Codec<A> {

    private final MapCodec<K> keyCodec;
    private final Function<K, DataResult<Codec<V>>> valueCodecs;
    private final Function<A, Pair<K, V>> decompose;
    private final BiFunction<K, V, DataResult<A>> construct;

    public MapDispatchedCodec(MapCodec<K> keyCodec, Function<K, DataResult<Codec<V>>> valueCodecs, Function<A, Pair<K, V>> decompose, BiFunction<K, V, DataResult<A>> construct) {
        this.keyCodec = keyCodec;
        this.valueCodecs = valueCodecs;
        this.decompose = decompose;
        this.construct = construct;
    }
    
    @Override
    public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
        Pair<K, V> pair = this.decompose.apply(input);
        return this.valueCodecs.apply(pair.getFirst())
                .mapError(err -> "No value codec available for " + pair.getFirst() + ": " + err)
                .flatMap(valueCodec -> valueCodec
                        .encode(pair.getSecond(), ops, prefix)
                        .mapError(err -> "Could not encode base element for key " + pair.getFirst() + ": " + err)
                )
                .flatMap(encoded -> ops.getMap(encoded)
                        .mapError(err -> "Map dispatched base codec encoded a value with is not a MapLike for key " + pair.getFirst())
                )
                .flatMap(base -> this.keyCodec.keys(ops)
                        .filter(key -> base.get(key) != null).findFirst()
                        .map(dupKey -> DataResult.<MapLike<T>>error("Key was encoded by base codec: " + dupKey + " (for " + pair.getFirst() + ")"))
                        .orElseGet(() -> DataResult.success(base))
                )
                .flatMap(base -> {
                    RecordBuilder<T> keys = this.keyCodec.encode(pair.getFirst(), ops, ops.mapBuilder());
                    return keys.build(ops.empty())
                            .mapError(err -> "Failed to build key map in map dispatched codec for key " + pair.getFirst() + ": " + err)
                            .flatMap(keyMap -> ops.mergeToMap(keyMap, base)
                                    .mapError(err -> "Failed to merge base and key in map dispatched codec for " + pair.getFirst() + ": " + err)
                            );
                });
    }

    @Override
    public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
        return ops.getMap(input)
                .mapError(err -> "Input is not a MapLike")
                .flatMap(map -> this.keyCodec.decode(ops, map)
                        .mapError(err -> "Failed to decode key: " + err)
                        .map(key -> Pair.of(key, map))
                )
                .flatMap(pair -> this.valueCodecs.apply(pair.getFirst())
                        .mapError(err -> "No value codec available for " + pair.getFirst() + ": " + err)
                        .flatMap(valueCodec -> ops.mergeToMap(ops.emptyMap(), pair.getSecond())
                                .flatMap(merged -> valueCodec.decode(ops, merged))
                                .mapError(err -> "Failed to decode dispatched value for key " + pair.getFirst() + ": " + err)
                        )
                        .flatMap(result -> this.construct.apply(pair.getFirst(), result.getFirst())
                                .mapError(err -> "Failed to construct result for key " + pair.getFirst() + ": " + err)
                                .map(constructed -> Pair.of(constructed, result.getSecond()))
                        )
                );
    }
}

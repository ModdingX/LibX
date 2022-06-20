package org.moddingx.libx.impl.codec;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.RecordBuilder;
import org.moddingx.libx.codec.ElementFactory;

import java.util.Set;
import java.util.stream.Collectors;

public class ExcludeEncoder<A> implements Encoder<A> {

    private final Encoder<A> encoder;
    private final ElementFactory factory;
    
    public ExcludeEncoder(Encoder<A> encoder, ElementFactory factory) {
        this.encoder = encoder;
        this.factory = factory;
    }
    
    @Override
    public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
        return this.encoder.encode(input, ops, prefix)
                .flatMap(ops::getMap)
                .flatMap(map -> {
                    Set<T> excluded = this.factory.elements(ops).collect(Collectors.toUnmodifiableSet());
                    RecordBuilder<T> builder = ops.mapBuilder();
                    map.entries()
                            .filter(entry -> !excluded.contains(entry.getFirst()))
                            .forEach(entry -> builder.add(entry.getFirst(), entry.getSecond()));
                    return builder.build(ops.empty());
                });
    }
}

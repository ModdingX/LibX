package io.github.noeppi_noeppi.libx.annotation.impl;

import com.google.common.collect.ImmutableMap;
import io.github.noeppi_noeppi.libx.util.LazyImmutableMap;
import io.github.noeppi_noeppi.libx.util.LazyValue;

import java.util.Map;
import java.util.function.Supplier;

public class LazyMapBuilder<K, V> {

    private final ImmutableMap.Builder<K, LazyValue<V>> builder = ImmutableMap.builder();

    public void put(K k, Supplier<V> v) {
        this.builder.put(k, new LazyValue<>(v));
    }

    public Map<K, V> build() {
        return new LazyImmutableMap<>(this.builder.build());
    }
}
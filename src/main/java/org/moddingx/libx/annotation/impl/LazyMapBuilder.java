package org.moddingx.libx.annotation.impl;

import com.google.common.collect.ImmutableMap;
import org.moddingx.libx.util.lazy.LazyImmutableMap;
import org.moddingx.libx.util.lazy.LazyValue;

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

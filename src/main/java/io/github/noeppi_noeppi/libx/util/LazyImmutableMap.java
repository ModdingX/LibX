package io.github.noeppi_noeppi.libx.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.LazyValue;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class LazyImmutableMap<K, V> implements Map<K, V> {

    private final ImmutableMap<K, LazyValue<V>> map;
    private final LazyValue<Collection<V>> values;
    private final LazyValue<Set<Entry<K, V>>> entries;

    public LazyImmutableMap(ImmutableMap<K, LazyValue<V>> map) {
        this.map = map;
        //noinspection UnstableApiUsage
        this.values = new LazyValue<>(() -> map.values().stream().map(LazyValue::getValue).collect(ImmutableList.toImmutableList()));
        //noinspection UnstableApiUsage
        this.entries = new LazyValue<>(() -> map.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue().getValue())).collect(ImmutableSet.toImmutableSet()));
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    @Override
    @Deprecated
    public boolean containsValue(Object value) {
        // We need to resolve all values here
        // Should be avoided
        for (LazyValue<V> lazy : this.map.values()) {
            if (lazy.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        return this.map.get(key).getValue();
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(@Nonnull Map m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public Set<K> keySet() {
        return this.map.keySet();
    }

    @Nonnull
    @Override
    public Collection<V> values() {
        return this.values.getValue();
    }

    @Nonnull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.entries.getValue();
    }
}

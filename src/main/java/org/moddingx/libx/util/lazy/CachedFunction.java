package org.moddingx.libx.util.lazy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Wraps a pure function. This makes sure, that the given function is only called once for
 * each key. The computed values will then be stored to avoid recomputing them.
 */
public class CachedFunction<T, R> implements Function<T, R> {

    private final Function<T, R> function;
    private final Map<T, R> cache = new HashMap<>();

    public CachedFunction(Function<T, R> function) {
        this.function = function;
    }

    @Override
    public R apply(T t) {
        return this.cache.computeIfAbsent(t, this.function);
    }
}

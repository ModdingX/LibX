package org.moddingx.libx.util;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * A lazy value that is resolved when it is first used. However, this one can be invalidated,
 * which causes it to recompute the value next time.
 */
public class CachedValue<T> {
    
    private final Supplier<? extends T> supplier;
    private T value;
    private boolean valid;

    /**
     * Creates a new cached value.
     */
    public CachedValue(@Nonnull Supplier<? extends T> supplier) {
        this.supplier = supplier;
        this.value = null;
        this.valid = false;
    }

    /**
     * Gets the value. If currently invalid, computes the value.
     */
    public T get() {
        if (!this.valid) {
            this.value = this.supplier.get();
            this.valid = true;
        }
        return this.value;
    }

    /**
     * Invalidates the cached value.
     */
    public void invalidate() {
        this.valid = false;
    }
}

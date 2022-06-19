package org.moddingx.libx.util.lazy;

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
     * Returns a new cached value. If this cached value is currently valid, the new cached value
     * will include the currently valid value. After that both values can be invalidated independent
     * of each other.
     */
    public CachedValue<T> copy() {
        CachedValue<T> copy = new CachedValue<>(this.supplier);
        if (this.valid) {
            copy.value = this.value;
            copy.valid = true;
        }
        return copy;
    }

    /**
     * Invalidates the cached value.
     */
    public void invalidate() {
        this.valid = false;
    }
}

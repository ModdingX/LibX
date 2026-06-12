package org.moddingx.libx.util.lazy;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A lazy value that is resolved when it is first used.
 */
public class LazyValue<T> {

    private Supplier<? extends T> supplier;
    private T value;
    private Exception evaluationException;

    /**
     * Creates a new lazy value. the supplier will be called once when
     * the value is first retrieved.
     */
    public LazyValue(@Nonnull Supplier<? extends T> supplier) {
        this.supplier = supplier;
        this.value = null;
        this.evaluationException = null;
    }

    /**
     * Gets the value. If not yet resolved, resolves the lazy value.
     */
    public T get() {
        if (this.supplier != null) {
            try {
                this.value = this.supplier.get();
                this.supplier = null;
            } catch (Exception e) {
                this.value = null;
                this.evaluationException = e;
            } finally {
                this.supplier = null;
            }
        }
        if (this.evaluationException != null) {
            throw new IllegalStateException("Evaluation of LazyValue failed", this.evaluationException);
        } else {
            return this.value;
        }
    }

    /**
     * Gets a new lazy value that will hold the value of this lazy value applied to
     * the mapper function. The mapper function will also get called lazy.
     */
    public <U> LazyValue<U> map(Function<T, U> mapper) {
        return new LazyValue<>(() -> mapper.apply(this.get()));
    }
}

package org.moddingx.libx.util.lazy;

import net.minecraft.util.LazyLoadedValue;
import net.minecraftforge.common.util.LazyOptional;

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
     * Wraps a {@link LazyLoadedValue} to a lazy value.
     */
    @SuppressWarnings("deprecation")
    public static <T> LazyValue<T> wrap(LazyLoadedValue<T> value) {
        return new LazyValue<>(value::get);
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
     * Gets a lazy value that will have the value of the lazy optional if present. If not
     * it will have the value of this lazy value.
     */
    public LazyValue<T> asDefault(LazyOptional<T> optional) {
        return new LazyValue<>(() -> optional.resolve().orElseGet(this::get));
    }

    /**
     * Gets a new lazy value that will hold the value of this lazy value applied to
     * the mapper function. The mapper function will also get called lazy.
     */
    public <U> LazyValue<U> map(Function<T, U> mapper) {
        return new LazyValue<>(() -> mapper.apply(this.get()));
    }
}

package io.github.noeppi_noeppi.libx.util;

import net.minecraftforge.common.util.LazyOptional;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A lazy value that is resolved when it is first used.
 */
public class LazyValue<T> {

    private Supplier<T> supplier;
    private T value;

    /**
     * Creates a new lazy value. the supplier will be called once when
     * the value is first retrieved.
     */
    public LazyValue(Supplier<T> supplier) {
        this.supplier = supplier;
        this.value = null;
    }

    /**
     * Wraps a {@link net.minecraft.util.LazyValue} to a lazy value.
     */
    // TODO currently weird javadoc, will change after the mapping update (LazyValue -> LazyLoadedValue), remove fully qualified names then.
    public static <T> LazyValue<T> wrap(net.minecraft.util.LazyLoadedValue<T> value) {
        return new LazyValue<>(value::get);
    }

    /**
     * Gets the value. If not yet resolved, resolves the lazy value.
     */
    public T get() {
        if (this.supplier != null) {
            this.value = this.supplier.get();
            this.supplier = null;
        }
        return this.value;
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

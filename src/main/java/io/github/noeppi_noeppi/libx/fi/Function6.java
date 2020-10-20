package io.github.noeppi_noeppi.libx.fi;

import java.util.Objects;
import java.util.function.Function;

/**
 * A function that takes 6 parameters and returns a value.
 */
public interface Function6<A, B, C, D, E, F, R> {

    R apply(A a, B b, C c, D d, E e, F f);

    default <V> Function6<A, B, C, D, E, F, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c, D d, E e, F f) -> after.apply(this.apply(a, b, c, d, e, f));
    }
}

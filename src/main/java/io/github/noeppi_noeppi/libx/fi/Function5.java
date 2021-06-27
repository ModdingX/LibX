package io.github.noeppi_noeppi.libx.fi;

import java.util.Objects;
import java.util.function.Function;

/**
 * A function that takes 5 parameters and returns a value.
 */
@FunctionalInterface
public interface Function5<A, B, C, D, E, R> {

    R apply(A a, B b, C c, D d, E e);

    default <V> Function5<A, B, C, D, E, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c, D d, E e) -> after.apply(this.apply(a, b, c, d, e));
    }
}

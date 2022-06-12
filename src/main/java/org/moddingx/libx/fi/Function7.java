package org.moddingx.libx.fi;

import java.util.Objects;
import java.util.function.Function;

/**
 * A function that takes 7 parameters and returns a value.
 */
@FunctionalInterface
public interface Function7<A, B, C, D, E, F, G, R> {

    R apply(A a, B b, C c, D d, E e, F f, G g);

    default <V> Function7<A, B, C, D, E, F, G, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c, D d, E e, F f, G g) -> after.apply(this.apply(a, b, c, d, e, f, g));
    }
}

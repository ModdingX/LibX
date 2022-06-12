package org.moddingx.libx.fi;

import java.util.Objects;
import java.util.function.Function;

/**
 * A function that takes 8 parameters and returns a value.
 */
@FunctionalInterface
public interface Function8<A, B, C, D, E, F, G, H, R> {

    R apply(A a, B b, C c, D d, E e, F f, G g, H h);

    default <V> Function8<A, B, C, D, E, F, G, H, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c, D d, E e, F f, G g, H h) -> after.apply(this.apply(a, b, c, d, e, f, g, h));
    }
}

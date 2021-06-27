package io.github.noeppi_noeppi.libx.fi;

import java.util.Objects;

/**
 * A consumer that takes 6 parameters.
 */
@FunctionalInterface
public interface Consumer6<A, B, C, D, E, F> {

    void apply(A a, B b, C c, D d, E e, F f);

    default Consumer6<A, B, C, D, E, F> andThen(Consumer6<A, B, C, D, E, F> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c, D d, E e, F f) -> { this.apply(a, b, c, d, e, f); after.apply(a, b, c, d, e, f); };
    }
}

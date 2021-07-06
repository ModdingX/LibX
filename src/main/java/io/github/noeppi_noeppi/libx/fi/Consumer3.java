package io.github.noeppi_noeppi.libx.fi;

import java.util.Objects;

/**
 * A consumer that takes 3 parameters.
 */
@FunctionalInterface
public interface Consumer3<A, B, C> {

    void apply(A a, B b, C c);

    default Consumer3<A, B, C> andThen(Consumer3<A, B, C> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c) -> { this.apply(a, b, c); after.apply(a, b, c); };
    }
}

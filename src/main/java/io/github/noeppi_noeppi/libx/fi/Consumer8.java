package io.github.noeppi_noeppi.libx.fi;

import java.util.Objects;

/**
 * A consumer that takes 8 parameters.
 */
@FunctionalInterface
public interface Consumer8<A, B, C, D, E, F, G, H> {

    void apply(A a, B b, C c, D d, E e, F f, G g, H h);

    default Consumer8<A, B, C, D, E, F, G, H> andThen(Consumer8<A, B, C, D, E, F, G, H> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c, D d, E e, F f, G g, H h) -> { this.apply(a, b, c, d, e, f, g, h); after.apply(a, b, c, d, e, f, g, h); };
    }
}

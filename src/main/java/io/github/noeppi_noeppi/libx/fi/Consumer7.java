package io.github.noeppi_noeppi.libx.fi;

import java.util.Objects;

/**
 * A consumer that takes 7 parameters.
 */
@FunctionalInterface
public interface Consumer7<A, B, C, D, E, F, G> {

    void apply(A a, B b, C c, D d, E e, F f, G g);

    default Consumer7<A, B, C, D, E, F, G> andThen(Consumer7<A, B, C, D, E, F, G> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c, D d, E e, F f, G g) -> { this.apply(a, b, c, d, e, f, g); after.apply(a, b, c, d, e, f, g); };
    }
}

package org.moddingx.libx.fi;

import java.util.Objects;

/**
 * A consumer that takes 5 parameters.
 */
@FunctionalInterface
public interface Consumer5<A, B, C, D, E> {

    void apply(A a, B b, C c, D d, E e);

    default Consumer5<A, B, C, D, E> andThen(Consumer5<A, B, C, D, E> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c, D d, E e) -> { this.apply(a, b, c, d, e); after.apply(a, b, c, d, e); };
    }
}

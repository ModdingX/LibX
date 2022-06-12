package org.moddingx.libx.fi;

import java.util.Objects;

/**
 * A consumer that takes 4 parameters.
 */
@FunctionalInterface
public interface Consumer4<A, B, C, D> {

    void apply(A a, B b, C c, D d);

    default Consumer4<A, B, C, D> andThen(Consumer4<A, B, C, D> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c, D d) -> { this.apply(a, b, c, d); after.apply(a, b, c, d); };
    }
}

package io.github.noeppi_noeppi.libx.fi;

import java.util.Objects;

/**
 * A predicate that takes 3 parameters.
 */
@FunctionalInterface
public interface Predicate3<A, B, C> {

    boolean test(A a, B b, C c);

    default Predicate3<A, B, C> and(Predicate3<A, B, C> other) {
        Objects.requireNonNull(other);
        return (A a, B b, C c) -> this.test(a, b, c) && other.test(a, b, c);
    }
    
    default Predicate3<A, B, C> or(Predicate3<A, B, C> other) {
        Objects.requireNonNull(other);
        return (A a, B b, C c) -> this.test(a, b, c) || other.test(a, b, c);
    }
    
    default Predicate3<A, B, C> negate() {
        return (A a, B b, C c) -> !this.test(a, b, c);
    }
}

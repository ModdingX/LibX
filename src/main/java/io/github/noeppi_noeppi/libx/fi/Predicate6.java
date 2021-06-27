package io.github.noeppi_noeppi.libx.fi;

import java.util.Objects;

/**
 * A predicate that takes 6 parameters.
 */
@FunctionalInterface
public interface Predicate6<A, B, C, D, E, F> {

    boolean test(A a, B b, C c, D d, E e, F f);

    default Predicate6<A, B, C, D, E, F> and(Predicate6<A, B, C, D, E, F> other) {
        Objects.requireNonNull(other);
        return (A a, B b, C c, D d, E e, F f) -> this.test(a, b, c, d, e, f) && other.test(a, b, c, d, e, f);
    }
    
    default Predicate6<A, B, C, D, E, F> or(Predicate6<A, B, C, D, E, F> other) {
        Objects.requireNonNull(other);
        return (A a, B b, C c, D d, E e, F f) -> this.test(a, b, c, d, e, f) || other.test(a, b, c, d, e, f);
    }
    
    default Predicate6<A, B, C, D, E, F> negate() {
        return (A a, B b, C c, D d, E e, F f) -> !this.test(a, b, c, d, e, f);
    }
}

package io.github.noeppi_noeppi.libx.fi;

import java.util.Objects;

/**
 * A predicate that takes 5 parameters.
 */
@FunctionalInterface
public interface Predicate5<A, B, C, D, E> {

    boolean test(A a, B b, C c, D d, E e);

    default Predicate5<A, B, C, D, E> and(Predicate5<A, B, C, D, E> other) {
        Objects.requireNonNull(other);
        return (A a, B b, C c, D d, E e) -> this.test(a, b, c, d, e) && other.test(a, b, c, d, e);
    }
    
    default Predicate5<A, B, C, D, E> or(Predicate5<A, B, C, D, E> other) {
        Objects.requireNonNull(other);
        return (A a, B b, C c, D d, E e) -> this.test(a, b, c, d, e) || other.test(a, b, c, d, e);
    }
    
    default Predicate5<A, B, C, D, E> negate() {
        return (A a, B b, C c, D d, E e) -> !this.test(a, b, c, d, e);
    }
}

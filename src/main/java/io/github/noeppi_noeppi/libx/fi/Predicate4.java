package io.github.noeppi_noeppi.libx.fi;

import java.util.Objects;

/**
 * A predicate that takes 4 parameters.
 */
@FunctionalInterface
public interface Predicate4<A, B, C, D> {

    boolean test(A a, B b, C c, D d);

    default Predicate4<A, B, C, D> and(Predicate4<A, B, C, D> other) {
        Objects.requireNonNull(other);
        return (A a, B b, C c, D d) -> this.test(a, b, c, d) && other.test(a, b, c, d);
    }
    
    default Predicate4<A, B, C, D> or(Predicate4<A, B, C, D> other) {
        Objects.requireNonNull(other);
        return (A a, B b, C c, D d) -> this.test(a, b, c, d) || other.test(a, b, c, d);
    }
    
    default Predicate4<A, B, C, D> negate() {
        return (A a, B b, C c, D d) -> !this.test(a, b, c, d);
    }
}

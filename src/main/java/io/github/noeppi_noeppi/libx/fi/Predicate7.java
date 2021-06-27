package io.github.noeppi_noeppi.libx.fi;

import java.util.Objects;

/**
 * A predicate that takes 7 parameters.
 */
@FunctionalInterface
public interface Predicate7<A, B, C, D, E, F, G> {

    boolean test(A a, B b, C c, D d, E e, F f, G g);

    default Predicate7<A, B, C, D, E, F, G> and(Predicate7<A, B, C, D, E, F, G> other) {
        Objects.requireNonNull(other);
        return (A a, B b, C c, D d, E e, F f, G g) -> this.test(a, b, c, d, e, f, g) && other.test(a, b, c, d, e, f, g);
    }
    
    default Predicate7<A, B, C, D, E, F, G> or(Predicate7<A, B, C, D, E, F, G> other) {
        Objects.requireNonNull(other);
        return (A a, B b, C c, D d, E e, F f, G g) -> this.test(a, b, c, d, e, f, g) || other.test(a, b, c, d, e, f, g);
    }
    
    default Predicate7<A, B, C, D, E, F, G> negate() {
        return (A a, B b, C c, D d, E e, F f, G g) -> !this.test(a, b, c, d, e, f, g);
    }
}

package io.github.noeppi_noeppi.libx.fi;

import java.util.Objects;

/**
 * A predicate that takes 8 parameters.
 */
@FunctionalInterface
public interface Predicate8<A, B, C, D, E, F, G, H> {

    boolean test(A a, B b, C c, D d, E e, F f, G g, H h);

    default Predicate8<A, B, C, D, E, F, G, H> and(Predicate8<A, B, C, D, E, F, G, H> other) {
        Objects.requireNonNull(other);
        return (A a, B b, C c, D d, E e, F f, G g, H h) -> this.test(a, b, c, d, e, f, g, h) && other.test(a, b, c, d, e, f, g, h);
    }
    
    default Predicate8<A, B, C, D, E, F, G, H> or(Predicate8<A, B, C, D, E, F, G, H> other) {
        Objects.requireNonNull(other);
        return (A a, B b, C c, D d, E e, F f, G g, H h) -> this.test(a, b, c, d, e, f, g, h) || other.test(a, b, c, d, e, f, g, h);
    }
    
    default Predicate8<A, B, C, D, E, F, G, H> negate() {
        return (A a, B b, C c, D d, E e, F f, G g, H h) -> !this.test(a, b, c, d, e, f, g, h);
    }
}

package org.moddingx.libx.fi;

import java.util.Objects;
import java.util.function.*;

/**
 * A function that takes 7 parameters and returns a value.
 */
@FunctionalInterface
public interface Function7<A, B, C, D, E, F, G, R> {

    R apply(A a, B b, C c, D d, E e, F f, G g);

    default <V> Function7<A, B, C, D, E, F, G, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c, D d, E e, F f, G g) -> after.apply(this.apply(a, b, c, d, e, f, g));
    }

    default Function6<B, C, D, E, F, G, R> partial(A a) {
        return (b, c, d, e, f, g) -> this.apply(a, b, c, d, e, f, g);
    }

    default Function5<C, D, E, F, G, R> partial(A a, B b) {
        return (c, d, e, f, g) -> this.apply(a, b, c, d, e, f, g);
    }

    default Function4<D, E, F, G, R> partial(A a, B b, C c) {
        return (d, e, f, g) -> this.apply(a, b, c, d, e, f, g);
    }

    default Function3<E, F, G, R> partial(A a, B b, C c, D d) {
        return (e, f, g) -> this.apply(a, b, c, d, e, f, g);
    }

    default BiFunction<F, G, R> partial(A a, B b, C c, D d, E e) {
        return (f, g) -> this.apply(a, b, c, d, e, f, g);
    }

    default Function<G, R> partial(A a, B b, C c, D d, E e, F f) {
        return g -> this.apply(a, b, c, d, e, f, g);
    }

    default Supplier<R> partial(A a, B b, C c, D d, E e, F f, G g) {
        return () -> this.apply(a, b, c, d, e, f, g);
    }
}

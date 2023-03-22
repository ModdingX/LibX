package org.moddingx.libx.fi;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A function that takes 5 parameters and returns a value.
 */
@FunctionalInterface
public interface Function5<A, B, C, D, E, R> {

    R apply(A a, B b, C c, D d, E e);

    default <V> Function5<A, B, C, D, E, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c, D d, E e) -> after.apply(this.apply(a, b, c, d, e));
    }

    default Function4<B, C, D, E, R> partial(A a) {
        return (b, c, d, e) -> this.apply(a, b, c, d, e);
    }

    default Function3<C, D, E, R> partial(A a, B b) {
        return (c, d, e) -> this.apply(a, b, c, d, e);
    }

    default BiFunction<D, E, R> partial(A a, B b, C c) {
        return (d, e) -> this.apply(a, b, c, d, e);
    }

    default Function<E, R> partial(A a, B b, C c, D d) {
        return e -> this.apply(a, b, c, d, e);
    }

    default Supplier<R> partial(A a, B b, C c, D d, E e) {
        return () -> this.apply(a, b, c, d, e);
    }
}

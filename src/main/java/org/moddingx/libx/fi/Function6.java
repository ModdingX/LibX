package org.moddingx.libx.fi;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A function that takes 6 parameters and returns a value.
 */
@FunctionalInterface
public interface Function6<A, B, C, D, E, F, R> {

    R apply(A a, B b, C c, D d, E e, F f);

    default <V> Function6<A, B, C, D, E, F, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c, D d, E e, F f) -> after.apply(this.apply(a, b, c, d, e, f));
    }

    default Function5<B, C, D, E, F, R> partial(A a) {
        return (b, c, d, e, f) -> this.apply(a, b, c, d, e, f);
    }

    default Function4<C, D, E, F, R> partial(A a, B b) {
        return (c, d, e, f) -> this.apply(a, b, c, d, e, f);
    }

    default Function3<D, E, F, R> partial(A a, B b, C c) {
        return (d, e, f) -> this.apply(a, b, c, d, e, f);
    }

    default BiFunction<E, F, R> partial(A a, B b, C c, D d) {
        return (e, f) -> this.apply(a, b, c, d, e, f);
    }

    default Function<F, R> partial(A a, B b, C c, D d, E e) {
        return f -> this.apply(a, b, c, d, e, f);
    }

    default Supplier<R> partial(A a, B b, C c, D d, E e, F f) {
        return () -> this.apply(a, b, c, d, e, f);
    }
}

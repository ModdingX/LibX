package org.moddingx.libx.fi;

import java.util.Objects;
import java.util.function.*;

/**
 * A function that takes 4 parameters and returns a value.
 */
@FunctionalInterface
public interface Function4<A, B, C, D, R> {

    R apply(A a, B b, C c, D d);

    default <V> Function4<A, B, C, D, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c, D d) -> after.apply(this.apply(a, b, c, d));
    }

    default Function3<B, C, D, R> partial(A a) {
        return (b, c, d) -> this.apply(a, b, c, d);
    }

    default BiFunction<C, D, R> partial(A a, B b) {
        return (c, d) -> this.apply(a, b, c, d);
    }

    default Function<D, R> partial(A a, B b, C c) {
        return d -> this.apply(a, b, c, d);
    }

    default Supplier<R> partial(A a, B b, C c, D d) {
        return () -> this.apply(a, b, c, d);
    }
}

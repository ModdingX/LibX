package org.moddingx.libx.fi;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A function that takes 3 parameters and returns a value.
 */
@FunctionalInterface
public interface Function3<A, B, C, R> {

    R apply(A a, B b, C c);

    default <V> Function3<A, B, C, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c) -> after.apply(this.apply(a, b, c));
    }
    
    default BiFunction<B, C, R> partial(A a) {
        return (b, c) -> this.apply(a, b, c);
    }
    
    default Function<C, R> partial(A a, B b) {
        return c -> this.apply(a, b, c);
    }

    default Supplier<R> partial(A a, B b, C c) {
        return () -> this.apply(a, b, c);
    }
}

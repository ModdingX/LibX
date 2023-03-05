package org.moddingx.libx.fi;

import java.util.Objects;
import java.util.function.*;

/**
 * A function that takes 8 parameters and returns a value.
 */
@FunctionalInterface
public interface Function8<A, B, C, D, E, F, G, H, R> {

    R apply(A a, B b, C c, D d, E e, F f, G g, H h);

    default <V> Function8<A, B, C, D, E, F, G, H, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c, D d, E e, F f, G g, H h) -> after.apply(this.apply(a, b, c, d, e, f, g, h));
    }

    default Function7<B, C, D, E, F, G, H, R> partial(A a) {
        return (b, c, d, e, f, g, h) -> this.apply(a, b, c, d, e, f, g, h);
    }

    default Function6<C, D, E, F, G, H, R> partial(A a, B b) {
        return (c, d, e, f, g, h) -> this.apply(a, b, c, d, e, f, g, h);
    }

    default Function5<D, E, F, G, H, R> partial(A a, B b, C c) {
        return (d, e, f, g, h) -> this.apply(a, b, c, d, e, f, g, h);
    }

    default Function4<E, F, G, H, R> partial(A a, B b, C c, D d) {
        return (e, f, g, h) -> this.apply(a, b, c, d, e, f, g, h);
    }

    default Function3<F, G, H, R> partial(A a, B b, C c, D d, E e) {
        return (f, g, h) -> this.apply(a, b, c, d, e, f, g, h);
    }

    default BiFunction<G, H, R> partial(A a, B b, C c, D d, E e, F f) {
        return (g, h) -> this.apply(a, b, c, d, e, f, g, h);
    }

    default Function<H, R> partial(A a, B b, C c, D d, E e, F f, G g) {
        return h -> this.apply(a, b, c, d, e, f, g, h);
    }

    default Supplier<R> partial(A a, B b, C c, D d, E e, F f, G g, H h) {
        return () -> this.apply(a, b, c, d, e, f, g, h);
    }
}

package org.moddingx.libx.fi;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A consumer that takes 6 parameters.
 */
@FunctionalInterface
public interface Consumer6<A, B, C, D, E, F> {

    void apply(A a, B b, C c, D d, E e, F f);

    default Consumer6<A, B, C, D, E, F> andThen(Consumer6<A, B, C, D, E, F> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c, D d, E e, F f) -> { this.apply(a, b, c, d, e, f); after.apply(a, b, c, d, e, f); };
    }

    default Consumer5<B, C, D, E, F> partial(A a) {
        return (b, c, d, e, f) -> this.apply(a, b, c, d, e, f);
    }

    default Consumer4<C, D, E, F> partial(A a, B b) {
        return (c, d, e, f) -> this.apply(a, b, c, d, e, f);
    }

    default Consumer3<D, E, F> partial(A a, B b, C c) {
        return (d, e, f) -> this.apply(a, b, c, d, e, f);
    }

    default BiConsumer<E, F> partial(A a, B b, C c, D d) {
        return (e, f) -> this.apply(a, b, c, d, e, f);
    }

    default Consumer<F> partial(A a, B b, C c, D d, E e) {
        return f -> this.apply(a, b, c, d, e, f);
    }

    default Runnable partial(A a, B b, C c, D d, E e, F f) {
        return () -> this.apply(a, b, c, d, e, f);
    }
}

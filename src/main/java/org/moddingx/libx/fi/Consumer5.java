package org.moddingx.libx.fi;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A consumer that takes 5 parameters.
 */
@FunctionalInterface
public interface Consumer5<A, B, C, D, E> {

    void apply(A a, B b, C c, D d, E e);

    default Consumer5<A, B, C, D, E> andThen(Consumer5<A, B, C, D, E> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c, D d, E e) -> { this.apply(a, b, c, d, e); after.apply(a, b, c, d, e); };
    }

    default Consumer4<B, C, D, E> partial(A a) {
        return (b, c, d, e) -> this.apply(a, b, c, d, e);
    }

    default Consumer3<C, D, E> partial(A a, B b) {
        return (c, d, e) -> this.apply(a, b, c, d, e);
    }

    default BiConsumer<D, E> partial(A a, B b, C c) {
        return (d, e) -> this.apply(a, b, c, d, e);
    }

    default Consumer<E> partial(A a, B b, C c, D d) {
        return e -> this.apply(a, b, c, d, e);
    }

    default Runnable partial(A a, B b, C c, D d, E e) {
        return () -> this.apply(a, b, c, d, e);
    }
}

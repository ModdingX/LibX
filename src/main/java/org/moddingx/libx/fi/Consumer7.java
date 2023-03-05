package org.moddingx.libx.fi;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A consumer that takes 7 parameters.
 */
@FunctionalInterface
public interface Consumer7<A, B, C, D, E, F, G> {

    void apply(A a, B b, C c, D d, E e, F f, G g);

    default Consumer7<A, B, C, D, E, F, G> andThen(Consumer7<A, B, C, D, E, F, G> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c, D d, E e, F f, G g) -> { this.apply(a, b, c, d, e, f, g); after.apply(a, b, c, d, e, f, g); };
    }

    default Consumer6<B, C, D, E, F, G> partial(A a) {
        return (b, c, d, e, f, g) -> this.apply(a, b, c, d, e, f, g);
    }

    default Consumer5<C, D, E, F, G> partial(A a, B b) {
        return (c, d, e, f, g) -> this.apply(a, b, c, d, e, f, g);
    }

    default Consumer4<D, E, F, G> partial(A a, B b, C c) {
        return (d, e, f, g) -> this.apply(a, b, c, d, e, f, g);
    }

    default Consumer3<E, F, G> partial(A a, B b, C c, D d) {
        return (e, f, g) -> this.apply(a, b, c, d, e, f, g);
    }

    default BiConsumer<F, G> partial(A a, B b, C c, D d, E e) {
        return (f, g) -> this.apply(a, b, c, d, e, f, g);
    }

    default Consumer<G> partial(A a, B b, C c, D d, E e, F f) {
        return g -> this.apply(a, b, c, d, e, f, g);
    }

    default Runnable partial(A a, B b, C c, D d, E e, F f, G g) {
        return () -> this.apply(a, b, c, d, e, f, g);
    }
}

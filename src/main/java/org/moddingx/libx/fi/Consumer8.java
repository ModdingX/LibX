package org.moddingx.libx.fi;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A consumer that takes 8 parameters.
 */
@FunctionalInterface
public interface Consumer8<A, B, C, D, E, F, G, H> {

    void apply(A a, B b, C c, D d, E e, F f, G g, H h);

    default Consumer8<A, B, C, D, E, F, G, H> andThen(Consumer8<A, B, C, D, E, F, G, H> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c, D d, E e, F f, G g, H h) -> { this.apply(a, b, c, d, e, f, g, h); after.apply(a, b, c, d, e, f, g, h); };
    }

    default Consumer7<B, C, D, E, F, G, H> partial(A a) {
        return (b, c, d, e, f, g, h) -> this.apply(a, b, c, d, e, f, g, h);
    }

    default Consumer6<C, D, E, F, G, H> partial(A a, B b) {
        return (c, d, e, f, g, h) -> this.apply(a, b, c, d, e, f, g, h);
    }

    default Consumer5<D, E, F, G, H> partial(A a, B b, C c) {
        return (d, e, f, g, h) -> this.apply(a, b, c, d, e, f, g, h);
    }

    default Consumer4<E, F, G, H> partial(A a, B b, C c, D d) {
        return (e, f, g, h) -> this.apply(a, b, c, d, e, f, g, h);
    }

    default Consumer3<F, G, H> partial(A a, B b, C c, D d, E e) {
        return (f, g, h) -> this.apply(a, b, c, d, e, f, g, h);
    }

    default BiConsumer<G, H> partial(A a, B b, C c, D d, E e, F f) {
        return (g, h) -> this.apply(a, b, c, d, e, f, g, h);
    }

    default Consumer<H> partial(A a, B b, C c, D d, E e, F f, G g) {
        return h -> this.apply(a, b, c, d, e, f, g, h);
    }

    default Runnable partial(A a, B b, C c, D d, E e, F f, G g, H h) {
        return () -> this.apply(a, b, c, d, e, f, g, h);
    }
}

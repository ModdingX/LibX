package org.moddingx.libx.fi;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A consumer that takes 4 parameters.
 */
@FunctionalInterface
public interface Consumer4<A, B, C, D> {

    void apply(A a, B b, C c, D d);

    default Consumer4<A, B, C, D> andThen(Consumer4<A, B, C, D> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c, D d) -> { this.apply(a, b, c, d); after.apply(a, b, c, d); };
    }

    default Consumer3<B, C, D> partial(A a) {
        return (b, c, d) -> this.apply(a, b, c, d);
    }
    
    default BiConsumer<C, D> partial(A a, B b) {
        return (c, d) -> this.apply(a, b, c, d);
    }

    default Consumer<D> partial(A a, B b, C c) {
        return d -> this.apply(a, b, c, d);
    }

    default Runnable partial(A a, B b, C c, D d) {
        return () -> this.apply(a, b, c, d);
    }
}

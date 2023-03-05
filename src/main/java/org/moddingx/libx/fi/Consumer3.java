package org.moddingx.libx.fi;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A consumer that takes 3 parameters.
 */
@FunctionalInterface
public interface Consumer3<A, B, C> {

    void apply(A a, B b, C c);

    default Consumer3<A, B, C> andThen(Consumer3<A, B, C> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c) -> { this.apply(a, b, c); after.apply(a, b, c); };
    }
    
    default BiConsumer<B, C> partial(A a) {
        return (b, c) -> this.apply(a, b, c);
    }
    
    default Consumer<C> partial(A a, B b) {
        return c -> this.apply(a, b, c);
    }

    default Runnable partial(A a, B b, C c) {
        return () -> this.apply(a, b, c);
    }
}

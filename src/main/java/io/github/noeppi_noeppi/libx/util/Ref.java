package io.github.noeppi_noeppi.libx.util;

/**
 * A wrapper for an element that implements {@link Object#equals(Object) equals} and
 * {@link Object#hashCode() hashCode} for reference equality, no matter what the actual
 * implementation of these methods does for the value of the {@code Ref} object.
 */
public record Ref<T>(T value) {

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Ref<?> ref && this.value() == ref.value();
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this.value());
    }

    @Override
    public String toString() {
        return "Ref[" + this.value() + "]";
    }
}

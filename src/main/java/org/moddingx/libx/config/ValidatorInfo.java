package org.moddingx.libx.config;

import javax.annotation.Nullable;
import java.lang.reflect.Proxy;

/**
 * Interface to retrieve information about a validator used on a config element
 */
public interface ValidatorInfo<T> {

    /**
     * Gets the annotation class used. This is the class of the annotation, not
     * of the {@link Proxy} that implements it at runtime.
     */
    @Nullable
    Class<T> type();

    /**
     * Gets the annotation used.
     */
    @Nullable
    T value();

    /**
     * Gets the annotation used, if it matches the given annotation class or null
     * if not.
     */
    @Nullable
    default <A> A value(Class<A> cls) {
        T t = this.value();
        if (t != null && cls.isAssignableFrom(t.getClass())) {
            //noinspection unchecked
            return (A) t;
        } else {
            return null;
        }
    }

    /**
     * Gets whether this object matches the validators type and is not corrected by
     * the validator.
     */
    boolean isValid(Object value);

    static <T> ValidatorInfo<T> empty() {
        return new ValidatorInfo<>() {

            @Nullable
            @Override
            public Class<T> type() {
                return null;
            }

            @Nullable
            @Override
            public T value() {
                return null;
            }

            @Override
            public boolean isValid(Object value) {
                return true;
            }
        };
    }
}

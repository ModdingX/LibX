package org.moddingx.libx.config.mapper;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * A factory that creates a {@link ValueMapper} by the generic type of a config key.
 */
public interface MapperFactory<T> {

    /**
     * The base class for which this factory creates mappers.
     */
    Class<T> type();

    /**
     * Creates a new {@link ValueMapper} from the give {@link Context}.
     */
    @Nullable
    ValueMapper<T, ?> create(Context ctx);
    
    interface Context {

        /**
         * The generic type of the config key.
         */
        Type getGenericType();

        /**
         * Wraps a {@link GenericValueMapper} into a regular {@link ValueMapper} given a mapper for
         * the generic element type.
         */
        <T, C> ValueMapper<T, ?> wrap(GenericValueMapper<T, ?, C> mapper, ValueMapper<C, ?> child);
    }
}

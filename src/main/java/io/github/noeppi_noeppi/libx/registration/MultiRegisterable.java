package io.github.noeppi_noeppi.libx.registration;

import io.github.noeppi_noeppi.libx.annotation.meta.SuperChainRequired;
import net.minecraft.core.Holder;

/**
 * Base interface for classes that act as a container for other objects. A {@link MultiRegisterable}
 * can't be directly registered into a registry, however the registry specified with it is used as
 * default in {@link #buildAdditionalRegisters(RegistrationContext, EntryCollector)}.
 */
public interface MultiRegisterable<T> {

    /**
     * Registers additional objects. These objects will be registered with the id and registry of
     * this object and optionally a name suffix.
     */
    @SuperChainRequired
    default void buildAdditionalRegisters(RegistrationContext ctx, EntryCollector<T> builder) {
        
    }

    /**
     * Interface to collect additional objects that are registered together with a {@link MultiRegisterable}.
     */
    interface EntryCollector<T> {

        /**
         * Registers a new object together with the current object.
         */
        void register(T value);

        /**
         * Registers a new object together with the current object.
         *
         * @param name A name suffix for the object.
         */
        void registerNamed(String name, T value);

        /**
         * Registers a new {@link MultiRegisterable} together with the current one.
         */
        void registerMulti(MultiRegisterable<T> value);

        /**
         * Registers a new {@link MultiRegisterable} together with the current one.
         *
         * @param name A name suffix for the object.
         */
        void registerMultiNamed(String name, MultiRegisterable<T> value);

        /**
         * Same as {@link #register(Object)} but creates a {@link Holder} for the object.
         */
        Holder<T> createHolder(T value);
        
        /**
         * Same as {@link #registerNamed(String, Object)} but creates a {@link Holder} for the object.
         */
        Holder<T> createNamedHolder(String name, T value);
    }
}

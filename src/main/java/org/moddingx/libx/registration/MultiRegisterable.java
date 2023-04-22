package org.moddingx.libx.registration;

import org.moddingx.libx.annotation.meta.SuperChainRequired;
import org.moddingx.libx.registration.tracking.RegistryTracker;

/**
 * Base interface for classes that act as a container for other objects. A {@link MultiRegisterable}
 * can't be directly registered into a registry, however the registry specified with it is used as
 * default in {@link #registerAdditional(RegistrationContext, EntryCollector)}.
 */
public interface MultiRegisterable<T> {

    /**
     * Registers additional objects. These objects will be registered with the id and registry of
     * this object and optionally a name suffix.
     */
    @SuperChainRequired
    default void registerAdditional(RegistrationContext ctx, EntryCollector<T> builder) {
        
    }

    /**
     * Adds fields with additional registry values to the {@link RegistryTracker}. This called, unless
     * {@link RegistrationBuilder#disableRegistryTracking()} ()} is set.
     */
    @SuperChainRequired
    default void initTracking(RegistrationContext ctx, Registerable.TrackingCollector builder) throws ReflectiveOperationException {

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
    }
}

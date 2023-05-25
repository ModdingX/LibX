package org.moddingx.libx.registration;

import net.minecraft.world.item.Item;
import org.moddingx.libx.annotation.meta.RemoveIn;
import org.moddingx.libx.annotation.meta.SuperChainRequired;
import org.moddingx.libx.annotation.registration.PlainRegisterable;
import org.moddingx.libx.annotation.registration.RegisterClass;
import org.moddingx.libx.registration.tracking.RegistryTracker;

/**
 * Base interface for classes that act as a container for other objects. A {@link MultiRegisterable}
 * can't be directly registered into a registry, however the registry specified with it is used as
 * default in {@link #registerAdditional(RegistrationContext, EntryCollector)}.
 * 
 * @deprecated To register multiple elements at the same time, use a regular {@link Registerable}
 * add register the additional elements in {@link Registerable#registerAdditional(RegistrationContext, Registerable.EntryCollector)}.
 * For the use in ModInit, use {@link PlainRegisterable}. This way, it's possible to for example put {@link Item}s and
 * containers registering 16 items, one for each color in the same class annotated ith {@link RegisterClass}.
 */
@Deprecated(forRemoval = true)
@RemoveIn(minecraft = "1.20")
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
     * 
     * @deprecated {@link MultiRegisterable} is deprecated. See there for more information
     */
    @Deprecated(forRemoval = true)
    @RemoveIn(minecraft = "1.20")
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

package org.moddingx.libx.registration;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.moddingx.libx.annotation.meta.SuperChainRequired;
import org.moddingx.libx.mod.ModXRegistration;

import javax.annotation.Nullable;

/**
 * Everything that is registered to {@link ModXRegistration} that implements this can specify dependencies
 * and things to be done during setup phase.
 */
public interface Registerable {
    
    /**
     * Do stuff needed in the common setup phase. This is called during parallel mod loading.
     */
    default void setupCommon(SetupContext ctx) {
        
    }
    
    /**
     * Do stuff needed in the client setup phase. This is called during parallel mod loading.
     */
    default void setupClient(SetupContext ctx) {
        
    }

    /**
     * Registers additional objects. Those may be {@link Registerable} as well. These objects will be registered
     * with the id of this object and optionally a name suffix.
     */
    @SuperChainRequired
    default void registerAdditional(RegistrationContext ctx, EntryCollector builder) {
        
    }
    
    /**
     * Similar to {@link #registerAdditional(RegistrationContext, EntryCollector)} but only invoked on the client.
     */
    @SuperChainRequired
    default void registerClientAdditional(RegistrationContext ctx, EntryCollector builder) {
        
    }

    /**
     * Interface to collect additional objects that are registered together with a {@link Registerable}.
     */
    interface EntryCollector {

        /**
         * Registers a new object together with the current one.
         */
        <T> void register(@Nullable ResourceKey<? extends Registry<T>> registry, T value);

        /**
         * Registers a new object together with the current one.
         * 
         * @param name A name suffix for the object.
         */
        <T> void registerNamed(@Nullable ResourceKey<? extends Registry<T>> registry, String name, T value);
    }
}

package org.moddingx.libx.registration;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.IForgeRegistry;
import org.moddingx.libx.annotation.meta.SuperChainRequired;
import org.moddingx.libx.mod.ModXRegistration;
import org.moddingx.libx.registration.tracking.RegistryTracker;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.function.Consumer;

/**
 * Everything that is registered to {@link ModXRegistration} that implements this can specify dependencies
 * and things to be done during setup phase.
 */
public interface Registerable {
    
    /**
     * Do stuff needed in the common setup phase. This is called during parallel mod loading.
     */
    default void registerCommon(SetupContext ctx) {
        
    }
    
    /**
     * Do stuff needed in the client setup phase. This is called during parallel mod loading.
     */
    @OnlyIn(Dist.CLIENT)
    default void registerClient(SetupContext ctx) {
        
    }

    /**
     * Registers additional objects. Those may be {@link Registerable} as well. These objects will be registered
     * with the id of this object and optionally a name suffix.
     */
    @SuperChainRequired
    default void registerAdditional(RegistrationContext ctx, EntryCollector builder) {
        
    }

    /**
     * Adds fields with additiona lregistry values to the {@link RegistryTracker}. This is only called, if
     * {@link RegistrationBuilder#enableRegistryTracking()} is set.
     */
    @SuperChainRequired
    default void initTracking(RegistrationContext ctx, TrackingCollector builder) throws ReflectiveOperationException {
        
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
        
        /**
         * Registers a new {@link MultiRegisterable} together with the current object.
         */
        <T> void registerMulti(@Nullable ResourceKey<? extends Registry<T>> registry, MultiRegisterable<T> value);
        
        /**
         * Registers a new {@link MultiRegisterable} together with the current object.
         * 
         * @param name A name suffix for the object.
         */
        <T> void registerMultiNamed(@Nullable ResourceKey<? extends Registry<T>> registry, String name, MultiRegisterable<T> value);
    }

    /**
     * Some helpful methods to track elements with names depending on this elements registry name.
     */
    interface TrackingCollector {

        /**
         * Tracks a field with a value with the same registry name as the current object, registered in the given
         * registry that is stored in the given field. The field must not be static and must be a field of the
         * class that implements {@link Registerable}
         */
        void track(IForgeRegistry<?> registry, Field field);
        
        /**
         * Tracks a field with a value with the same registry name as the current object with a given suffix,
         * registered in the given registry that is stored in the given field. The field must not be static
         * and must be a field of the class that implements {@link Registerable}
         */
        void trackNamed(IForgeRegistry<?> registry, String name, Field field);
        
        /**
         * Adds a registry tracking action with the same registry name as the current object.
         */
        <T> void run(IForgeRegistry<T> registry, Consumer<T> action);
        
        /**
         * Adds a registry tracking action with the same registry name as the current object with a given suffix.
         */
        <T> void runNamed(IForgeRegistry<T> registry, String name, Consumer<T> action);
    }
}

package org.moddingx.libx.registration;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
    
    @SuperChainRequired
    default void initTracking(RegistrationContext ctx) {
        
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

        /**
         * Same as {@link #register(ResourceKey, Object)} but creates a {@link Holder} for the object.
         */
        <T> Holder<T> createHolder(@Nullable ResourceKey<? extends Registry<T>> registry, T value);

        /**
         * Same as {@link #registerNamed(ResourceKey, String, Object)} but creates a {@link Holder} for the object.
         */
        <T> Holder<T> createNamedHolder(@Nullable ResourceKey<? extends Registry<T>> registry, String name, T value);
    }
}

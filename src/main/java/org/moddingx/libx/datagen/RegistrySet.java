package org.moddingx.libx.datagen;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;

/**
 * Provides access to datagen registries.
 */
public interface RegistrySet {

    /**
     * Gets a registry from the registry set.
     */
    <T> Registry<T> registry(ResourceKey<? extends Registry<T>> registryKey);
    
    /**
     * Gets a writable registry from the registry set. This only succeeds, if the registry is a datapack registry
     * and this method is called in the correct {@link DatagenStage stage}.
     * 
     * @see DatagenStage
     */
    <T> WritableRegistry<T> writableRegistry(ResourceKey<? extends Registry<T>> registryKey);

    /**
     * Gets a {@link RegistryAccess} for this registry set. This can only be used during the
     * {@link DatagenStage#DATAGEN datagen stage}.
     */
    RegistryAccess registryAccess();
}

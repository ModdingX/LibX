package io.github.noeppi_noeppi.libx.registration.resolution;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;

/**
 * A registry resolver is used to resolve a registry by a {@link ResourceKey}.
 */
public interface RegistryResolver {
    
    <T> Optional<ResolvedRegistry<T>> resolve(ResourceKey<? extends Registry<T>> key);
}

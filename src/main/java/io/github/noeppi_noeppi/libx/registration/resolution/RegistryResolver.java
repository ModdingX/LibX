package io.github.noeppi_noeppi.libx.registration.resolution;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;

public interface RegistryResolver {
    
    <T> Optional<ResolvedRegistry<T>> resolve(ResourceKey<? extends Registry<T>> key);
}

package io.github.noeppi_noeppi.libx.impl.registration.resolution;

import io.github.noeppi_noeppi.libx.registration.resolution.RegistryResolver;
import io.github.noeppi_noeppi.libx.registration.resolution.ResolvedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;

public class VanillaRegistryResolver implements RegistryResolver {

    private final Registry<? extends Registry<?>> rootRegistry;

    public VanillaRegistryResolver(Registry<? extends Registry<?>> rootRegistry) {
        this.rootRegistry = rootRegistry;
    }

    @Override
    public <T> Optional<ResolvedRegistry<T>> resolve(ResourceKey<? extends Registry<T>> key) {
        if (this.rootRegistry.key().location().equals(key.location())) return Optional.empty();
        //noinspection unchecked
        return (Optional<ResolvedRegistry<T>>) (Optional<?>) ((Registry<Object>) (Registry<?>) this.rootRegistry).getOptional((ResourceKey<Object>) (ResourceKey<?>) key);
    }
}

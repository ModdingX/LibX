package org.moddingx.libx.impl.registration.resolution;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.moddingx.libx.registration.resolution.RegistryResolver;
import org.moddingx.libx.registration.resolution.ResolvedRegistry;

import java.util.Optional;

public class ForgeRegistryResolver implements RegistryResolver {

    public static final ForgeRegistryResolver INSTANCE = new ForgeRegistryResolver();

    private ForgeRegistryResolver() {

    }
    
    @Override
    public <T> Optional<ResolvedRegistry<T>> resolve(ResourceKey<? extends Registry<T>> key) {
        IForgeRegistry<?> registry = RegistryManager.ACTIVE.getRegistry(key.location());
        if (registry == null) return Optional.empty();
        //noinspection unchecked
        return Optional.of(new ResolvedRegistry.Forge<>(ResourceKey.createRegistryKey(registry.getRegistryName()), (Class<T>) registry.getRegistrySuperType()));
    }
}

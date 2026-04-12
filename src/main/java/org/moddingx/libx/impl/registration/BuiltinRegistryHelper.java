package org.moddingx.libx.impl.registration;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.stream.Stream;

public class BuiltinRegistryHelper {

    // Only use this, if you know what you are doing. The game expects datapack registries to be present here, which they aren't.
    public static final HolderLookup.Provider BUILTIN_REGISTRY_LOOKUP = new HolderLookup.Provider() {

        @Nonnull
        @Override
        public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
            return BuiltInRegistries.REGISTRY.registryKeySet().stream().map(key -> (ResourceKey<? extends Registry<?>>) key);
        }

        @Nonnull
        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(@Nonnull ResourceKey<? extends Registry<? extends T>> registryKey) {
            return BuiltInRegistries.REGISTRY.getOptional((ResourceKey) registryKey);
        }
    };
}

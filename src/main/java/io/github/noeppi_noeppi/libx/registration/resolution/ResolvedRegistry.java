package io.github.noeppi_noeppi.libx.registration.resolution;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;

public sealed interface ResolvedRegistry<T> permits ResolvedRegistry.Forge, ResolvedRegistry.Vanilla {
    
    ResourceKey<? extends Registry<T>> registryKey();
    
    @Nullable
    default Class<T> superType() {
        return null;
    }

    default Holder<T> createHolder(ResourceKey<T> key) {
        throw new IllegalStateException("The registry " + this.getClass().getSimpleName() + "[" + this.registryKey() + "] can't be used to create holders");
    }
    
    record Forge<T>(ResourceKey<? extends Registry<T>> registryKey, Class<T> superType) implements ResolvedRegistry<T> {}
    
    record Vanilla<T>(Registry<T> registry) implements ResolvedRegistry<T> {
        
        @Override
        public ResourceKey<? extends Registry<T>> registryKey() {
            return this.registry().key();
        }

        @Override
        public Holder<T> createHolder(ResourceKey<T> key) {
            try {
                return this.registry().getOrCreateHolder(key);
            } catch(IllegalStateException e) {
                return ResolvedRegistry.super.createHolder(key);
            }
        }
    }
}

package org.moddingx.libx.registration.resolution;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import javax.annotation.Nullable;

/**
 * A resolved registry that objects can be registered into.
 */
public sealed interface ResolvedRegistry<T> permits ResolvedRegistry.Forge, ResolvedRegistry.Vanilla {

    /**
     * Gets the registry key of the resolved registry.
     */
    ResourceKey<? extends Registry<T>> registryKey();

    /**
     * Gets the registry super type of the registry if available.
     */
    @Nullable
    default Class<T> superType() {
        return null;
    }

    /**
     * Creates a {@link Holder} from a given id on the resolved registry.
     * 
     * @throws IllegalStateException If the registry does not support creating holders in advance to registering.
     */
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

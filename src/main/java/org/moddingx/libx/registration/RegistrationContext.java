package org.moddingx.libx.registration;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.moddingx.libx.mod.ModXRegistration;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

/**
 * Provides some data that is present during registration like the id and registry key of the object
 * being registered.
 */
public sealed class RegistrationContext permits SetupContext {

    private final ModXRegistration mod;
    private final ResourceLocation id;
    private final Optional<ResourceKey<?>> key;
    private final Optional<ResourceKey<? extends Registry<?>>> registry;
    
    public RegistrationContext(ModXRegistration mod, ResourceLocation id, @Nullable ResourceKey<?> key) {
        this.mod = mod;
        this.id = id;
        this.key = Optional.ofNullable(key);
        this.registry = this.key.map(ResourceKey::registry).map(ResourceKey::createRegistryKey);
        if (this.key.isPresent() && !Objects.equals(this.id, this.key.get().location())) {
            throw new IllegalArgumentException("Id does not match resource key: " + id + " " + key);
        }
    }
    
    /**
     * Gets the mod to which object belongs.
     */
    public ModXRegistration mod() {
        return this.mod;
    }

    /**
     * Gets the id of the object being registered.
     */
    public ResourceLocation id() {
        return this.id;
    }

    /**
     * Gets the {@link ResourceKey} of the object being registered. If the object is registered
     * without a registry key, the {@link Optional} will be empty.
     */
    public Optional<ResourceKey<?>> key() {
        return this.key;
    }

    /**
     * Gets the registry key of the registry the object is registered in. If the object is registered
     * without a registry key, the {@link Optional} will be empty.
     */
    public Optional<ResourceKey<? extends Registry<?>>> registry() {
        return this.registry;
    }
}

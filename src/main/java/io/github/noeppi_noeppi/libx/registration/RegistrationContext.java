package io.github.noeppi_noeppi.libx.registration;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public sealed class RegistrationContext permits SetupContext {

    private final ResourceLocation id;
    private final Optional<ResourceKey<?>> key;
    private final Optional<ResourceKey<? extends Registry<?>>> registry;
    
    public RegistrationContext(ResourceLocation id, @Nullable ResourceKey<?> key) {
        this.id = id;
        this.key = Optional.ofNullable(key);
        this.registry = this.key.map(ResourceKey::registry).map(ResourceKey::createRegistryKey);
        if (this.key.isPresent() && !Objects.equals(this.id, this.key.get().location())) {
            throw new IllegalArgumentException("Id does not match resource key: " + id + " " + key);
        }
    }

    public ResourceLocation id() {
        return this.id;
    }

    public Optional<ResourceKey<?>> key() {
        return this.key;
    }
    
    public Optional<ResourceKey<? extends Registry<?>>> registry() {
        return this.registry;
    }
}

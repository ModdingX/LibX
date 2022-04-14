package io.github.noeppi_noeppi.libx.registration;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public final class RegistrationContext {

    private final ResourceLocation id;
    private final Optional<ResourceKey<?>> key;
    private final Optional<ResourceKey<? extends Registry<?>>> registry;
    private final Consumer<Runnable> enqueue;
    
    public RegistrationContext(ResourceLocation id, @Nullable ResourceKey<?> key, Consumer<Runnable> enqueue) {
        this.id = id;
        this.key = Optional.ofNullable(key);
        this.registry = this.key.map(ResourceKey::registry).map(ResourceKey::createRegistryKey);
        this.enqueue = enqueue;
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

    public void enqueue(Runnable action) {
        this.enqueue.accept(action);
    }
}

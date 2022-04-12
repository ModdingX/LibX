package io.github.noeppi_noeppi.libx.impl.registration;

import io.github.noeppi_noeppi.libx.mod.ModXRegistration;
import io.github.noeppi_noeppi.libx.registration.Registerable;
import io.github.noeppi_noeppi.libx.registration.RegistrationBuilder;
import io.github.noeppi_noeppi.libx.registration.resolution.RegistryResolver;
import io.github.noeppi_noeppi.libx.registration.resolution.ResolvedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.*;
import java.util.function.Supplier;

public class RegistrationDispatcher {
    
    private final Object LOCK = new Object();
    
    private final String modid;
    
    private final List<RegistryResolver> resolvers;
    private final Map<ResourceKey<? extends Registry<?>>, Optional<ResolvedRegistry<?>>> resolvedRegistries;
    
    private final Map<ResourceKey<? extends Registry<?>>, Map<ResourceKey<?>, Object>> forgeEntries;
    private final Map<Registry<?>, Map<ResourceKey<?>, Object>> vanillaEntries;
    
    public RegistrationDispatcher(String modid, RegistrationBuilder.Result result) {
        this.modid = modid;
        this.resolvers = result.resolvers();
        this.resolvedRegistries = new HashMap<>();
        this.forgeEntries = new HashMap<>();
        this.vanillaEntries = new HashMap<>();
    }
    
    private <T> Optional<ResolvedRegistry<T>> registry(ResourceKey<? extends Registry<T>> key) {
        synchronized (this.LOCK) {
            //noinspection unchecked
            return (Optional<ResolvedRegistry<T>>) (Optional<?>) this.resolvedRegistries.computeIfAbsent(key, k ->
                    (Optional<ResolvedRegistry<?>>) (Optional<?>) this.resolvers.stream().flatMap(resolver -> resolver.resolve(key).stream()).findFirst()
            );
        }
    }
    
    private <T> Supplier<Holder<T>> register(ResourceKey<? extends Registry<T>> registry, String id, T value) {
        synchronized (this.LOCK) {
            if (value instanceof Registerable registerable) {
                // TODO additional elements
            }
            ResourceKey<T> resourceKey = ResourceKey.create(registry, new ResourceLocation(this.modid, id));
            if (registry != ModXRegistration.ANY_REGISTRY) {
                ResolvedRegistry<T> resolved = this.registry(registry).orElseThrow(() -> new NoSuchElementException("Failed to resolve registry " + registry));
                if (resolved instanceof ResolvedRegistry.Forge<?>) {
                    this.addEntry(this.forgeEntries, registry, resourceKey, value);
                } else if (resolved instanceof ResolvedRegistry.Vanilla<T> vanilla) {
                    this.addEntry(this.vanillaEntries, vanilla.registry(), resourceKey, value);
                }
                return () -> resolved.createHolder(resourceKey);
            } else {
                return () -> {
                    throw new IllegalStateException("Can't create holders for the " + ModXRegistration.ANY_REGISTRY.location() + " registry");
                };
            }
        }
    }
    
    private <T> void addEntry(Map<T, Map<ResourceKey<?>, Object>> entries, T key, ResourceKey<?> resourceKey, Object element) {
        synchronized (this.LOCK) {
            entries.computeIfAbsent(key, k -> new HashMap<>()).put(resourceKey, element);
        }
    }
    
    public void registerForge(RegistryEvent.Register<? extends IForgeRegistryEntry<?>> event) {
        ResourceKey<? extends Registry<?>> key = ResourceKey.createRegistryKey(event.getName());
        Map<ResourceKey<?>, Object> map = this.forgeEntries.get(key);
        if (map != null) {
            for (Map.Entry<ResourceKey<?>, Object> entry : map.entrySet()) {
                if (!(entry.getValue() instanceof IForgeRegistryEntry<?>)) {
                    throw new IllegalStateException("Can't register object into forge registry that is not an instance of IForgeRegistryEntry: " + entry.getValue() + " (" + entry.getKey() + ")");
                } else if (!event.getRegistry().getRegistrySuperType().isAssignableFrom(entry.getValue().getClass())) {
                    throw new IllegalStateException("Can't register object into forge registry: Type mismatch: " + entry.getValue() + " (" + entry.getKey() + ") expected " + event.getRegistry().getRegistrySuperType() + ", got " + entry.getClass());
                } else {
                    ((IForgeRegistryEntry<?>) entry.getValue()).setRegistryName(entry.getKey().location());
                    //noinspection unchecked,rawtypes
                    ((IForgeRegistry) event.getRegistry()).register((IForgeRegistryEntry<?>) entry.getValue());
                }
            }
        }
    }
    
    public void registerVanilla() {
        synchronized (this.LOCK) {
            for (Map.Entry<Registry<?>, Map<ResourceKey<?>, Object>> registryEntry : this.vanillaEntries.entrySet()) {
                for (Map.Entry<ResourceKey<?>, Object> elementEntry : registryEntry.getValue().entrySet()) {
                    //noinspection unchecked
                    Registry.register((Registry<Object>) registryEntry.getKey(), (ResourceKey<Object>) elementEntry.getKey(), elementEntry.getValue());
                }
            }
        }
    }
}

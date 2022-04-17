package io.github.noeppi_noeppi.libx.impl.registration;

import io.github.noeppi_noeppi.libx.registration.*;
import io.github.noeppi_noeppi.libx.registration.resolution.RegistryResolver;
import io.github.noeppi_noeppi.libx.registration.resolution.ResolvedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RegistrationDispatcher {
    
    private final Object LOCK = new Object();
    
    private final String modid;
    
    private final List<RegistryResolver> resolvers;
    private final List<RegistryCondition> conditions;
    private final List<RegistryTransformer> transformers;
    private final Map<ResourceKey<? extends Registry<?>>, Optional<ResolvedRegistry<?>>> resolvedRegistries;
    
    private final Map<ResourceKey<? extends Registry<?>>, Map<ResourceKey<?>, Object>> forgeEntries;
    private final Map<Registry<?>, Map<ResourceKey<?>, Object>> vanillaEntries;
    private final List<NamedRegisterable> registerables;
    
    public RegistrationDispatcher(String modid, RegistrationBuilder.Result result) {
        this.modid = modid;
        this.resolvers = result.resolvers();
        this.conditions = result.conditions();
        this.transformers = result.transformers();
        this.resolvedRegistries = new HashMap<>();
        this.forgeEntries = new HashMap<>();
        this.vanillaEntries = new HashMap<>();
        this.registerables = new ArrayList<>();
    }
    
    private <T> Optional<ResolvedRegistry<T>> registry(ResourceKey<? extends Registry<T>> key) {
        synchronized (this.LOCK) {
            //noinspection unchecked
            return (Optional<ResolvedRegistry<T>>) (Optional<?>) this.resolvedRegistries.computeIfAbsent(key, k ->
                    (Optional<ResolvedRegistry<?>>) (Optional<?>) this.resolvers.stream().flatMap(resolver -> resolver.resolve(key).stream()).findFirst()
            );
        }
    }
    
    public <T> Supplier<Holder<T>> register(@Nullable ResourceKey<? extends Registry<T>> registry, String id, T value) {
        synchronized (this.LOCK) {
            ResourceLocation rl = new ResourceLocation(this.modid, id);
            
            @Nullable
            ResourceKey<T> resourceKey = registry == null ? null : ResourceKey.create(registry, rl);
            
            RegistrationContext ctx = new RegistrationContext(new ResourceLocation(this.modid, id), registry);
            
            List<RegistryCondition> failedConditions = this.conditions.stream().filter(condition -> !condition.shouldRegister(ctx, value)).toList();
            if (!failedConditions.isEmpty()) {
                return () -> {
                    throw new IllegalStateException("Can't create holder, object not registered due to failed conditions: [ " + failedConditions.stream().map(Object::toString).collect(Collectors.joining(", ")) + " ]");
                };
            }
            
            EntryCollector collector = new EntryCollector(id);
            
            this.transformers.forEach(transformer -> transformer.buildAdditionalRegisters(ctx, value, collector));
            
            if (value instanceof Registerable registerable) {
                this.registerables.add(new NamedRegisterable(ctx, registerable));
                registerable.buildAdditionalRegisters(collector);
            }
            
            if (registry != null) {
                ResolvedRegistry<T> resolved = this.registry(registry).orElseThrow(() -> new NoSuchElementException("Failed to resolve registry " + registry));
                if (resolved instanceof ResolvedRegistry.Forge<?>) {
                    this.addEntry(this.forgeEntries, registry, resourceKey, value);
                } else if (resolved instanceof ResolvedRegistry.Vanilla<T> vanilla) {
                    this.addEntry(this.vanillaEntries, vanilla.registry(), resourceKey, value);
                }
                return () -> resolved.createHolder(resourceKey);
            } else {
                return () -> {
                    throw new IllegalStateException("Can't create holders without registry.");
                };
            }
        }
    }
    
    private <T> void addEntry(Map<T, Map<ResourceKey<?>, Object>> entries, T key, ResourceKey<?> resourceKey, Object element) {
        synchronized (this.LOCK) {
            Map<ResourceKey<?>, Object> entryMap = entries.computeIfAbsent(key, k -> new HashMap<>());
            if (entryMap.containsKey(resourceKey)) {
                throw new IllegalStateException("Duplicate element for registration: " + resourceKey + " with value " + element);
            } else {
                entryMap.put(resourceKey, element);
            }
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
    
    public void registerCommon(FMLCommonSetupEvent event) {
        this.registerables.forEach(reg -> reg.registerCommon(event::enqueueWork));
    }
    
    public void registerClient(FMLClientSetupEvent event) {
        this.registerables.forEach(reg -> reg.registerClient(event::enqueueWork));
    }
    
    private class EntryCollector implements Registerable.EntryCollector {

        private final String baseId;

        private EntryCollector(String baseId) {
            this.baseId = baseId;
        }

        @Override
        public <T> void register(@Nullable ResourceKey<? extends Registry<T>> registry, T value) {
            RegistrationDispatcher.this.register(registry, this.baseId, value);
        }

        @Override
        public <T> void registerNamed(@Nullable ResourceKey<? extends Registry<T>> registry, String name, T value) {
            RegistrationDispatcher.this.register(registry, this.baseId + "_" + name, value);
        }

        @Override
        public <T> Holder<T> createHolder(@Nullable ResourceKey<? extends Registry<T>> registry, T value) {
            return RegistrationDispatcher.this.register(registry, this.baseId, value).get();
        }

        @Override
        public <T> Holder<T> createNamedHolder(@Nullable ResourceKey<? extends Registry<T>> registry, String name, T value) {
            return RegistrationDispatcher.this.register(registry, this.baseId + "_" + name, value).get();
        }
    }
    
    private record NamedRegisterable(RegistrationContext ctx, Registerable value) {

        public void registerCommon(Consumer<Runnable> enqueue) {
            this.value().registerCommon(new SetupContext(this.ctx(), enqueue));
        }

        public void registerClient(Consumer<Runnable> enqueue) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> this.value().registerClient(new SetupContext(this.ctx(), enqueue)));
        }
    }
}

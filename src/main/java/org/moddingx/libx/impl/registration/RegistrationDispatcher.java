package org.moddingx.libx.impl.registration;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.moddingx.libx.impl.registration.tracking.TrackingInstance;
import org.moddingx.libx.registration.*;
import org.moddingx.libx.registration.resolution.RegistryResolver;
import org.moddingx.libx.registration.resolution.ResolvedRegistry;
import org.moddingx.libx.registration.tracking.RegistryTracker;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RegistrationDispatcher {
    
    private final Object LOCK = new Object();
    
    private final String modid;
    
    private final boolean trackingEnabled;
    private final List<RegistryResolver> resolvers;
    private final List<RegistryCondition> conditions;
    private final List<RegistryTransformer> transformers;
    
    private boolean hasRegistrationRun;
    private final List<Runnable> registrationHandlers;
    private final Map<ResourceKey<? extends Registry<?>>, Optional<ResolvedRegistry<?>>> resolvedRegistries;
    
    // TODO Merge all entries together and have unified way to create holders (forge first, then vanilla)
    //  Remove registry resolving
    private final Map<ResourceKey<? extends Registry<?>>, Map<ResourceKey<?>, Object>> forgeEntries;
    private final Map<ResourceKey<? extends Registry<?>>, Map<ResourceKey<?>, Object>> vanillaEntries;
    private final List<NamedRegisterable> registerables;
    
    public RegistrationDispatcher(String modid, RegistrationBuilder.Result result) {
        this.modid = modid;
        this.trackingEnabled = result.tracking();
        this.resolvers = result.resolvers();
        this.conditions = result.conditions();
        this.transformers = result.transformers();
        this.hasRegistrationRun = false;
        this.registrationHandlers = new ArrayList<>();
        this.resolvedRegistries = new HashMap<>();
        this.forgeEntries = new HashMap<>();
        this.vanillaEntries = new HashMap<>();
        this.registerables = new ArrayList<>();
    }
    
    private void runRegistration() {
        synchronized (this.LOCK) {
            if (this.hasRegistrationRun) {
                return;
            } else {
                this.hasRegistrationRun = true;
            }
        }
        // Must run registration handlers outside of synchronized block
        // so #register is not blocked.
        this.registrationHandlers.forEach(Runnable::run);
    }
    
    public void addRegistrationHandler(Runnable handler) {
        synchronized (this.LOCK) {
            this.registrationHandlers.add(handler);
        }
    }
    
    private <T> Optional<ResolvedRegistry<T>> registry(ResourceKey<? extends Registry<T>> key) {
        synchronized (this.LOCK) {
            //noinspection unchecked
            return (Optional<ResolvedRegistry<T>>) (Optional<?>) this.resolvedRegistries.computeIfAbsent(key, k ->
                    (Optional<ResolvedRegistry<?>>) (Optional<?>) this.resolvers.stream().flatMap(resolver -> resolver.resolve(key).stream()).findFirst()
            );
        }
    }
    
    public <T> void registerMulti(@Nullable ResourceKey<? extends Registry<T>> registry, String id, MultiRegisterable<T> value) {
        synchronized (this.LOCK) {
            ResourceLocation rl = new ResourceLocation(this.modid, id);
            @Nullable
            ResourceKey<T> resourceKey = registry == null ? null : ResourceKey.create(registry, rl);
            RegistrationContext ctx = new RegistrationContext(new ResourceLocation(this.modid, id), resourceKey);

            if (this.conditions.stream().allMatch(condition -> condition.shouldRegisterMulti(ctx, registry, value))) {
                MultiEntryCollector<T> collector = new MultiEntryCollector<>(this, registry, id);
                this.transformers.forEach(transformer -> transformer.transformMulti(ctx, registry, value, collector));
                value.registerAdditional(ctx, collector);

                if (this.trackingEnabled) {
                    try {
                        value.initTracking(ctx, new TrackingInstance(rl, value));
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("Failed to initialise multi registry tracking for " + id + " in " + registry + ": " + value, e);
                    }
                }
            }
        }
    }
    
    public <T> Supplier<Holder<T>> register(@Nullable ResourceKey<? extends Registry<T>> registry, String id, T value) {
        synchronized (this.LOCK) {
            if (value instanceof MultiRegisterable<?>) {
                throw new IllegalArgumentException("Can't register MultiRegistrable. Use #registerMulti instead: " + registry + "/" + this.modid + ":" + id + " @ " + value);
            }
            
            ResourceLocation rl = new ResourceLocation(this.modid, id);
            
            @Nullable
            ResourceKey<T> resourceKey = registry == null ? null : ResourceKey.create(registry, rl);
            RegistrationContext ctx = new RegistrationContext(rl, resourceKey);
            
            List<RegistryCondition> failedConditions = this.conditions.stream().filter(condition -> !condition.shouldRegister(ctx, value)).toList();
            if (!failedConditions.isEmpty()) {
                return () -> {
                    throw new IllegalStateException("Can't create holder, object not registered due to failed conditions: [ " + failedConditions.stream().map(Object::toString).collect(Collectors.joining(", ")) + " ]");
                };
            }
            
            SingleEntryCollector collector = new SingleEntryCollector(this, id);
            
            this.transformers.forEach(transformer -> transformer.transform(ctx, value, collector));
            
            if (value instanceof Registerable registerable) {
                this.registerables.add(new NamedRegisterable(ctx, registerable));
                registerable.registerAdditional(ctx, collector);
            }
            
            if (registry != null) {
                ResolvedRegistry<T> resolved = this.registry(registry).orElseThrow(() -> new NoSuchElementException("Failed to resolve registry " + registry));
                if (resolved instanceof ResolvedRegistry.Forge<?>) {
                    this.addEntry(this.forgeEntries, registry, resourceKey, value);
                } else if (resolved instanceof ResolvedRegistry.Vanilla<T>) {
                    this.addEntry(this.vanillaEntries, registry, resourceKey, value);
                }
                
                if (value instanceof Registerable registerable) {
                    if (this.trackingEnabled) {
                        try {
                            registerable.initTracking(ctx, new TrackingInstance(rl, value));
                        } catch (ReflectiveOperationException e) {
                            throw new IllegalStateException("Failed to initialise registry tracking for " + id + " in " + registry + ": " + value, e);
                        }
                    }
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
    
    public void registerBy(RegisterEvent event) {
        this.runRegistration();
        
//        IForgeRegistry<?> forgeRegistry = event.getForgeRegistry();
//        if (forgeRegistry != null) {
//            Map<ResourceKey<?>, Object> map = this.forgeEntries.get(event.getRegistryKey());
//            if (map != null) {
//                for (Map.Entry<ResourceKey<?>, Object> entry : map.entrySet()) {
//                    //noinspection unchecked,rawtypes
//                    ((IForgeRegistry) forgeRegistry).register(entry.getKey().location(), entry.getValue());
//                }
//            }
//        }
//        
//        Registry<?> vanillaRegistry = event.getVanillaRegistry();
//        if (vanillaRegistry != null) {
//            Map<ResourceKey<?>, Object> map = this.vanillaEntries.get(event.getRegistryKey());
//        }
        
        // TODO add when forge + vanilla entries are merged
    }
    
    public void registerCommon(FMLCommonSetupEvent event) {
        this.registerables.forEach(reg -> reg.registerCommon(event::enqueueWork));
    }
    
    public void registerClient(FMLClientSetupEvent event) {
        this.registerables.forEach(reg -> reg.registerClient(event::enqueueWork));
    }
    
    public void notifyRegisterField(IForgeRegistry<?> registry, String id, Field field) {
        if (this.trackingEnabled) {
            RegistryTracker.track(registry, field, new ResourceLocation(this.modid, id));
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

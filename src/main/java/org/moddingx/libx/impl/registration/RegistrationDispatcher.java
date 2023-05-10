package org.moddingx.libx.impl.registration;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.moddingx.libx.impl.registration.tracking.TrackingInstance;
import org.moddingx.libx.mod.ModXRegistration;
import org.moddingx.libx.registration.*;
import org.moddingx.libx.registration.tracking.RegistryTracker;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

public class RegistrationDispatcher {
    
    private final Object LOCK = new Object();
    
    private final ModXRegistration mod;
    
    private final boolean trackingEnabled;
    private final List<RegistryCondition> conditions;
    private final List<RegistryTransformer> transformers;
    
    private boolean hasRegistrationRun;
    private final List<Runnable> registrationHandlers;
    
    private final Map<ResourceKey<? extends Registry<?>>, RegistryData> allEntries;
    private final List<NamedRegisterable> registerables;
    
    public RegistrationDispatcher(ModXRegistration mod, RegistrationBuilder.Result result) {
        this.mod = mod;
        this.trackingEnabled = result.tracking();
        this.conditions = result.conditions();
        this.transformers = result.transformers();
        this.hasRegistrationRun = false;
        this.registrationHandlers = new ArrayList<>();
        this.allEntries = new HashMap<>();
        this.registerables = new LinkedList<>();
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
            if (this.hasRegistrationRun) {
                throw new IllegalStateException("Can't add a registration handler after the registration has run.");
            }
            this.registrationHandlers.add(handler);
        }
    }
    
    public <T> void registerMulti(@Nullable ResourceKey<? extends Registry<T>> registry, String id, MultiRegisterable<T> value) {
        synchronized (this.LOCK) {
            ResourceLocation rl = this.mod.resource(id);
            @Nullable ResourceKey<T> resourceKey = registry == null ? null : ResourceKey.create(registry, rl);
            RegistrationContext ctx = new RegistrationContext(this.mod, rl, resourceKey);

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
    
    public <T> void register(@Nullable ResourceKey<? extends Registry<T>> registry, String id, T value) {
        synchronized (this.LOCK) {
            if (value instanceof MultiRegisterable<?>) {
                throw new IllegalArgumentException("Can't register MultiRegistrable. Use #registerMulti instead: " + registry + "/" + this.mod.modid + ":" + id + " @ " + value);
            }
            
            ResourceLocation rl = this.mod.resource(id);
            @Nullable ResourceKey<T> resourceKey = registry == null ? null : ResourceKey.create(registry, rl);
            RegistrationContext ctx = new RegistrationContext(this.mod, rl, resourceKey);
            
            List<RegistryCondition> failedConditions = this.conditions.stream().filter(condition -> !condition.shouldRegister(ctx, value)).toList();
            if (!failedConditions.isEmpty()) return;
            
            SingleEntryCollector collector = new SingleEntryCollector(this, id);
            
            this.transformers.forEach(transformer -> transformer.transform(ctx, value, collector));
            
            if (registry != null) {
                this.addEntry(resourceKey, value);
            }
            
            if (value instanceof Registerable registerable) {
                this.registerables.add(new NamedRegisterable(ctx, registerable));
                registerable.registerAdditional(ctx, collector);
            }
            
            if (registry != null) {
                if (value instanceof Registerable registerable && this.trackingEnabled) {
                    try {
                        registerable.initTracking(ctx, new TrackingInstance(rl, value));
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("Failed to initialise registry tracking for " + id + " in " + registry + ": " + value, e);
                    }
                }
            }
        }
    }
    
    private void addEntry(ResourceKey<?> resourceKey, Object element) {
        synchronized (this.LOCK) {
            RegistryData data = this.allEntries.computeIfAbsent(ResourceKey.createRegistryKey(resourceKey.registry()), k -> new RegistryData());
            data.add(resourceKey, element);
        }
    }
    
    public void registerBy(RegisterEvent event) {
        this.runRegistration();
        
        RegistryData data = this.allEntries.get(event.getRegistryKey());
        if (data != null) {
            //noinspection unchecked
            event.register((ResourceKey<Registry<Object>>) event.getRegistryKey(), reg -> {
                for (Map.Entry<ResourceKey<?>, Object> entry : data.values()) {
                    reg.register(entry.getKey().location(), entry.getValue());
                }
            });
        }
    }
    
    public void registerCommon(FMLCommonSetupEvent event) {
        this.registerables.forEach(reg -> reg.registerCommon(event::enqueueWork));
    }
    
    public void registerClient(FMLClientSetupEvent event) {
        this.registerables.forEach(reg -> reg.registerClient(event::enqueueWork));
    }
    
    public void notifyRegisterField(IForgeRegistry<?> registry, String id, Field field) {
        if (this.trackingEnabled) {
            RegistryTracker.track(registry, field, this.mod.resource(id));
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

    private static final class RegistryData {

        private final Set<ResourceKey<?>> keys = new HashSet<>();
        private final List<Pair<ResourceKey<?>, Object>> values = new ArrayList<>();

        public void add(ResourceKey<?> key, Object value) {
            if (this.keys.contains(key)) {
                throw new IllegalStateException("Duplicate element for registration: " + key + " with value " + value);
            } else {
                this.keys.add(key);
                this.values.add(Pair.of(key, value));
            }
        }

        public List<Pair<ResourceKey<?>, Object>> values() {
            return Collections.unmodifiableList(this.values);
        }
    }
}

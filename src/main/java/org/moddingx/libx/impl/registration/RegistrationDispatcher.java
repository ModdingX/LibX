package org.moddingx.libx.impl.registration;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.moddingx.libx.impl.registration.handler.CapabilityRegistrationHandler;
import org.moddingx.libx.impl.registration.handler.ClientExtensionRegistrationHandler;
import org.moddingx.libx.mod.ModXRegistration;
import org.moddingx.libx.registration.*;
import org.moddingx.libx.registration.util.EnumObjects;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class RegistrationDispatcher {
    
    private final Object LOCK = new Object();
    
    private final ModXRegistration mod;
    
    private final List<RegistryCondition> conditions;
    private final List<RegistryTransformer> transformers;
    
    private boolean hasRegistrationRun;
    private final List<Runnable> registrationHandlers;
    
    private final Map<ResourceKey<? extends Registry<?>>, RegistryData> allEntries;
    private final List<NamedRegisterable> registerables;
    
    private final CapabilityRegistrationHandler capabilityHandler;
    @Nullable private final ClientExtensionRegistrationHandler clientExtHandler;
    
    public RegistrationDispatcher(ModXRegistration mod, RegistrationBuilder.Result result) {
        this.mod = mod;
        this.conditions = result.conditions();
        this.transformers = result.transformers();
        this.hasRegistrationRun = false;
        this.registrationHandlers = new ArrayList<>();
        this.allEntries = new HashMap<>();
        this.registerables = new LinkedList<>();
        this.capabilityHandler = new CapabilityRegistrationHandler(this::runRegistration);
        this.clientExtHandler = FMLEnvironment.getDist() == Dist.CLIENT ? new ClientExtensionRegistrationHandler(this::runRegistration) : null;
    }
    
    private void runRegistration() {
        synchronized (this.LOCK) {
            if (this.hasRegistrationRun) {
                return;
            } else {
                this.hasRegistrationRun = true;
            }
        }
        // Must run registration handlers outside synchronized block
        // so #register is not blocked.
        this.registrationHandlers.forEach(Runnable::run);
    }
    
    public void setupEventListeners(IEventBus modBus) {
        modBus.addListener(this::registerBy);
        modBus.addListener(this::registerCommon);
        modBus.addListener(this::registerClient);
        modBus.addListener(this.capabilityHandler::registerCapabilities);
        if (this.clientExtHandler != null) {
            modBus.addListener(this.clientExtHandler::registerClientExtensions);
            modBus.addListener(this.clientExtHandler::registerFluidModels);
            modBus.addListener(this.clientExtHandler::registerMenuScreens);
        }
    }
    
    public void addRegistrationHandler(Runnable handler) {
        synchronized (this.LOCK) {
            if (this.hasRegistrationRun) {
                throw new IllegalStateException("Can't add a registration handler after the registration has run.");
            }
            this.registrationHandlers.add(handler);
        }
    }
    
    public <T> void register(@Nullable ResourceKey<? extends Registry<T>> registry, String id, T value) {
        synchronized (this.LOCK) {
            Identifier identifier = this.mod.id(id);
            @Nullable ResourceKey<T> resourceKey = registry == null ? null : ResourceKey.create(registry, identifier);
            RegistrationContext ctx = new RegistrationContext(this.mod, identifier, resourceKey);
            
            List<RegistryCondition> failedConditions = this.conditions.stream().filter(condition -> !condition.shouldRegister(ctx, value)).toList();
            if (!failedConditions.isEmpty()) {
                if (registry != null) {
                    cleanupIntrusiveHolder(registry, value);
                } else if (value instanceof EnumObjects<?, ?> enumObject && enumObject.registryKey() != null) {
                    for (Object object : enumObject.values()) {
                        cleanupIntrusiveHolder((ResourceKey<? extends Registry<T>>) enumObject.registryKey(), (T) object);
                    }
                }
                return;
            }
            
            EntryCollectorImpl collector = new EntryCollectorImpl(this, id);
            
            this.transformers.forEach(transformer -> transformer.transform(ctx, value, collector));
            
            if (registry != null) {
                this.addEntry(resourceKey, value);
            }
            
            this.capabilityHandler.handle(identifier, value);
            if (this.clientExtHandler != null) {
                this.clientExtHandler.handle(identifier, value);
            }
            
            if (value instanceof Registerable registerable) {
                this.registerables.add(new NamedRegisterable(ctx, registerable));
                registerable.registerAdditional(ctx, collector);
                if (FMLEnvironment.getDist() == Dist.CLIENT) {
                    RegisterableClientAdapter.registerClientAdditional(registerable, ctx, collector);
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
    
    private void registerBy(RegisterEvent event) {
        this.runRegistration();
        
        RegistryData data = this.allEntries.get(event.getRegistryKey());
        if (data != null) {
            //noinspection unchecked
            event.register((ResourceKey<Registry<Object>>) event.getRegistryKey(), reg -> {
                for (Map.Entry<ResourceKey<?>, Object> entry : data.values()) {
                    reg.register(entry.getKey().identifier(), entry.getValue());
                }
            });
        }
    }

    private void registerCommon(FMLCommonSetupEvent event) {
        this.runRegistration();
        this.registerables.forEach(reg -> reg.registerCommon(event::enqueueWork));
    }

    private void registerClient(FMLClientSetupEvent event) {
        this.runRegistration();
        this.registerables.forEach(reg -> reg.registerClient(event::enqueueWork));
    }

    private static <T> void cleanupIntrusiveHolder(ResourceKey<? extends Registry<T>> registryKey, T value) {
        BuiltInRegistries.REGISTRY.get(registryKey.identifier()).ifPresent(registryHolder -> {
            if (registryHolder.value() instanceof MappedRegistry<?> mappedRegistry && mappedRegistry.unregisteredIntrusiveHolders != null) {
                ((MappedRegistry<T>) mappedRegistry).unregisteredIntrusiveHolders.remove(value);
            }
        });
    }

    private record NamedRegisterable(RegistrationContext ctx, Registerable value) {

        public void registerCommon(Consumer<Runnable> enqueue) {
            this.value().setupCommon(new SetupContext(this.ctx(), enqueue));
        }

        public void registerClient(Consumer<Runnable> enqueue) {
            if (FMLEnvironment.getDist() == Dist.CLIENT) {
                RegisterableClientAdapter.registerClient(this.value(), new SetupContext(this.ctx(), enqueue));
            }
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

    // Safely reference the client only methods from registerable
    private static class RegisterableClientAdapter {
        
        static {
            if (FMLEnvironment.getDist().isDedicatedServer()) {
                throw new IllegalStateException("RegisterableClientAdapter should never be loaded on the dedicated server. This is a bug in LibX.");
            }
        }
        
        public static void registerClient(Registerable registerable, SetupContext ctx) {
            registerable.setupClient(ctx);
        }
        
        public static void registerClientAdditional(Registerable registerable, RegistrationContext ctx, Registerable.EntryCollector builder) {
            registerable.registerClientAdditional(ctx, builder);
        }
    }
}

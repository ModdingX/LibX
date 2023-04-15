package org.moddingx.libx.impl.datagen;

import com.google.common.base.Stopwatch;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import org.moddingx.libx.LibX;
import org.moddingx.libx.datagen.*;
import org.moddingx.libx.impl.datagen.registries.DatagenRegistrySet;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class InternalDataProvider implements DataProvider {
    
    private final DatagenSystem system;
    private final DatagenRegistrySet rootRegistries;
    private final List<Entry<RegistryProvider>> registryProviders;
    private final List<Entry<RegistryProvider>> extensionProviders;
    private final List<Entry<DataProvider>> dataProviders;
    
    // A null value means the class exists multiple times
    private final Map<Class<? extends RegistryProvider>, RegistryProvider> initialisedRegistryProviders;
    private final Map<Class<? extends DataProvider>, DataProvider> initialisedDataProviders;

    public InternalDataProvider(DatagenSystem system, DatagenRegistrySet rootRegistries, List<Entry<RegistryProvider>> registryProviders, List<Entry<RegistryProvider>> extensionProviders, List<Entry<DataProvider>> dataProviders) {
        this.system = system;
        this.rootRegistries = rootRegistries;
        this.registryProviders = List.copyOf(registryProviders);
        this.extensionProviders = List.copyOf(extensionProviders);
        this.dataProviders = new ArrayList<>(dataProviders); // Must be mutable
        
        this.initialisedRegistryProviders = new HashMap<>();
        this.initialisedDataProviders = new HashMap<>();
    }

    @Nonnull
    @Override
    public final String getName() {
        return "LibX datagen for " + this.system.mod().modid;
    }

    @Nonnull
    @Override
    public CompletableFuture<?> run(@Nonnull CachedOutput output) {
        this.rootRegistries.transition(DatagenStage.REGISTRY_SETUP);
        LibX.logger.info("Start of stage {}", DatagenStage.REGISTRY_SETUP);
        
        // Initialise providers
        List<RegistryProvider> theRegistryProviders = new ArrayList<>();
        for (Entry<RegistryProvider> entry : this.registryProviders) {
            theRegistryProviders.add(this.initProvider(DatagenStage.REGISTRY_SETUP, entry, this.initialisedRegistryProviders));
        }
        
        // Run providers
        for (RegistryProvider provider : theRegistryProviders) {
            this.<Void>runTimed(provider.getName(), DatagenStage.REGISTRY_SETUP, () -> { provider.run(); return null; });
        }

        LibX.logger.info("End of stage {}", DatagenStage.REGISTRY_SETUP);
        this.rootRegistries.transition(DatagenStage.EXTENSION_SETUP);
        LibX.logger.info("Start of stage {}", DatagenStage.EXTENSION_SETUP);
        
        // Initialise providers
        List<RegistryProvider> theExtensionProviders = new ArrayList<>();
        for (Entry<RegistryProvider> entry : this.extensionProviders) {
            theExtensionProviders.add(this.initProvider(DatagenStage.EXTENSION_SETUP, entry, this.initialisedRegistryProviders));
        }
        
        // Run providers
        for (RegistryProvider provider : theExtensionProviders) {
            this.<Void>runTimed(provider.getName(), DatagenStage.EXTENSION_SETUP, () -> { provider.run(); return null; });
        }
        
        LibX.logger.info("End of stage {}", DatagenStage.EXTENSION_SETUP);
        this.rootRegistries.transition(DatagenStage.DATAGEN);
        LibX.logger.info("Start of stage {}", DatagenStage.DATAGEN);
        
        // Initialise providers
        List<DataProvider> theDataProviders = new ArrayList<>();
        // Can't use regular foreach loop as the list may grow while looping
        //noinspection ForLoopReplaceableByForEach
        for (int idx = 0; idx < this.dataProviders.size(); idx++) {
            theDataProviders.add(this.initProvider(DatagenStage.DATAGEN, this.dataProviders.get(idx), this.initialisedDataProviders));
        }
        
        // Run providers
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (DataProvider provider : theDataProviders) {
            futures.add(this.runTimed(provider.getName(), DatagenStage.EXTENSION_SETUP, () -> provider.run(output)));
        }
        
        LibX.logger.info("End of stage {}", DatagenStage.DATAGEN);
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }
    
    private <T> T initProvider(DatagenStage stage, Entry<T> entry, Map<Class<? extends T>, T> providerMap) {
        Context ctx = new Context(stage, entry.target());
        T provider = entry.factory().apply(ctx);
        ctx.canQuery = false;
        if (providerMap.containsKey(provider.getClass())) {
            //noinspection unchecked
            providerMap.put((Class<? extends T>) provider.getClass(), null);
        } else {
            //noinspection unchecked
            providerMap.put((Class<? extends T>) provider.getClass(), provider);
        }
        return provider;
    }
    
    private <T> T runTimed(String name, DatagenStage stage, Callable<T> action) {
        LibX.logger.info("Starting provider {} in stage {}", name, stage);
        Stopwatch stopwatch = Stopwatch.createStarted();
        T result;
        try {
            result = action.call();
        } catch (Exception e) {
            stopwatch.stop();
            LibX.logger.info("{} failed after {} ms", name, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            throw new RuntimeException(e);
        }
        stopwatch.stop();
        LibX.logger.info("{} finished after {} ms", name, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return result;
    }
    
    public record Entry<T>(PackTarget target, Function<DatagenContext, T> factory) {}
    
    private class Context extends DatagenContext {

        private boolean canQuery;
        
        protected Context(DatagenStage stage, PackTarget target) {
            super(stage, InternalDataProvider.this.system, target);
            this.canQuery = true;
        }

        @Override
        public <T extends RegistryProvider> T findRegistryProvider(Class<T> cls) {
            if (!this.canQuery) throw new IllegalStateException("Provider has already been set up, can't query additional providers");
            //noinspection unchecked
            return (T) Objects.requireNonNull(InternalDataProvider.this.initialisedRegistryProviders.get(cls), "Could not lookup provider: " + cls);
        }

        @Override
        public <T extends DataProvider> T findDataProvider(Class<T> cls) {
            if (!this.canQuery) throw new IllegalStateException("Provider has already been set up, can't query additional providers");
            //noinspection unchecked
            return (T) Objects.requireNonNull(InternalDataProvider.this.initialisedDataProviders.get(cls), "Could not lookup provider: " + cls);
        }

        @Override
        public void addAdditionalProvider(Function<DatagenContext, DataProvider> provider) {
            if (!this.canQuery) throw new IllegalStateException("Provider has already been set up, can't add additional providers");
            InternalDataProvider.this.dataProviders.add(new Entry<>(this.target(), provider));
        }
    }
}

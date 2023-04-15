package org.moddingx.libx.impl.datagen.registries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.datagen.DatagenSystem;
import org.moddingx.libx.datagen.PackTarget;
import org.moddingx.libx.datagen.RegistrySet;

import java.util.*;

public class DatagenRegistrySet implements RegistrySet {
    
    private final RegistryAccess rootAccess;
    private final DatagenRegistrySet root;
    private final List<DatagenRegistrySet> parents;
    private final List<DatagenRegistrySet> children;
            
    private DatagenStage stage;
    private final Map<ResourceKey<? extends Registry<?>>, DatagenRegistry<?>> registries;
    private RegistryAccess localAccess;
    
    public DatagenRegistrySet(RegistryAccess access) {
        this.rootAccess = access;
        this.root = this;
        this.parents = List.of();
        this.children = new ArrayList<>();
        this.stage = DatagenStage.REGISTRY_SETUP;
        this.registries = new HashMap<>();
        this.localAccess = null;
    }
    
    public DatagenRegistrySet(List<DatagenRegistrySet> parents) {
        if (parents.isEmpty()) throw new IllegalArgumentException("Registry set needs at least one parent");
        this.parents = List.copyOf(parents);
        this.children = new ArrayList<>();
        List<DatagenRegistrySet> roots = this.parents.stream().map(set -> set.root).distinct().toList();
        if (roots.size() != 1) throw new IllegalArgumentException("Registry set can only have a single root");
        this.root = roots.get(0);
        this.rootAccess = this.root.rootAccess;
        for (DatagenRegistrySet parent : this.parents) {
            if (parent.stage != DatagenStage.REGISTRY_SETUP) {
                throw new IllegalStateException("New registry sets ca only be created in registry setup phase");
            }
            parent.children.add(this);
        }
        this.stage = DatagenStage.REGISTRY_SETUP;
        this.registries = new HashMap<>();
        this.localAccess = null;
    }

    public boolean isRoot() {
        return this.root == this;
    }
    
    public List<DatagenRegistrySet> getDirectParents() {
        return this.parents;
    }

    public List<DatagenRegistrySet> getDirectChildren() {
        return Collections.unmodifiableList(this.children);
    }
    
    @Override
    public <T> Registry<T> registry(ResourceKey<? extends Registry<T>> registryKey) {
         Optional<DatagenRegistry<T>> writable = this.getDatagenRegistry(registryKey, false);
         if (writable.isPresent()) return writable.get();
         return this.rootAccess.registry(registryKey).orElseThrow(() -> new NoSuchElementException("Registry not known: " + registryKey));
    }
    
    @Override
    public <T> WritableRegistry<T> writableRegistry(ResourceKey<? extends Registry<T>> registryKey) {
        if (this.isRoot()) throw new IllegalStateException("The root registry set can't be used to query writeable registries");
        return this.getDatagenRegistry(registryKey, true).orElseThrow(() ->
                new IllegalStateException("Can't write to registry " + registryKey + " during " + this.stage + " phase")
        );
    }

    @Override
    public RegistryAccess registryAccess() {
        if (this.localAccess == null) throw new IllegalStateException("Can't query datagen registry access in " + this.stage + " stage.");
        return this.localAccess;
    }

    public <T> Optional<DatagenRegistry<T>> getDatagenRegistry(ResourceKey<? extends Registry<T>> registryKey, boolean forWrite) {
        if (forWrite && this.stage == DatagenStage.DATAGEN) return Optional.empty();
        Optional<RegistryDataLoader.RegistryData<?>> data = DatagenRegistryLoader.getDataPackRegistries(null).stream()
                .filter(rd -> Objects.equals(rd.key(), registryKey)).findFirst();
        if (data.isEmpty()) return Optional.empty();
        if (forWrite && DatagenSystem.extensionRegistries().contains(registryKey) != (this.stage == DatagenStage.EXTENSION_SETUP)) {
            return Optional.empty();
        }
        if (this.isRoot()) {
            // Root registry set: Inherit from the registry access instead of parents
            //noinspection unchecked
            return Optional.of((DatagenRegistry<T>) this.registries.computeIfAbsent(registryKey, k -> {
                //noinspection unchecked
                DatagenRegistry<T> reg = DatagenRegistry.createRoot(
                        registryKey, this, (Codec<T>) data.get().elementCodec(),
                        this.rootAccess.registry(registryKey).orElseThrow(() ->
                                new IllegalStateException("Could not setup " + registryKey + " registry: Root registry not available")
                        )
                );
                reg.freeze(); // Root registries may not be modified, instantly freeze them.
                return reg;
            }));
        } else {
            // Inherited registry set: Inherit from parents
            //noinspection unchecked
            return Optional.of((DatagenRegistry<T>) this.registries.computeIfAbsent(registryKey, k -> {
                //noinspection unchecked
                DatagenRegistry<T> reg = DatagenRegistry.create(
                        registryKey, this, (Codec<T>) data.get().elementCodec(),
                        this.getDirectParents().stream().map(parent ->
                                parent.getDatagenRegistry(registryKey, false).orElseThrow(() ->
                                        new IllegalStateException("Could not setup " + registryKey + " registry: Parent registry not available")
                                )
                        ).toList()
                );
                if (this.shouldBeFrozen(this.stage, registryKey)) {
                    reg.freeze();
                }
                return reg;
            }));
        }
    }
    
    public <T> Set<DatagenRegistry<T>> collectActiveChildRegistries(ResourceKey<? extends Registry<T>> registryKey) {
        Set<DatagenRegistry<T>> registries = new HashSet<>();
        this.addActiveChildRegistries(registryKey, registries);
        return Collections.unmodifiableSet(registries);
    }
    
    private  <T> void addActiveChildRegistries(ResourceKey<? extends Registry<T>> registryKey, Set<DatagenRegistry<T>> registries) {
        for (DatagenRegistrySet child : this.getDirectChildren()) {
            if (child.registries.containsKey(registryKey)) {
                //noinspection unchecked
                registries.add((DatagenRegistry<T>) child.registries.get(registryKey));
            }
            child.addActiveChildRegistries(registryKey, registries);
        }
    }
    
    public void transition(DatagenStage stage) {
        if (!this.isRoot()) throw new IllegalStateException("Stage transitions must happen on the root registry set");
        this.doTransition(stage);
    }
    
    private void doTransition(DatagenStage newStage) {
        DatagenStage oldStage = this.stage;
        if (oldStage.ordinal() == 0 && newStage.ordinal() == 0) return;
        if (oldStage.ordinal() + 1 != newStage.ordinal()) throw new IllegalArgumentException("Invalid transition: " + oldStage + " -> " + newStage);
        // Freeze all our own registries
        for (DatagenRegistry<?> registry : this.registries.values()) {
            if (this.shouldBeFrozen(newStage, registry.key())) {
                registry.freeze();
            }
        }
        // Mark transition as complete, so registries created for the local registry access instantly freeze
        // and we are detected as transitioned by children.
        this.stage = newStage;
        // If the new stage is DATAGEN, build the registry access
        if (newStage == DatagenStage.DATAGEN) {
            this.localAccess = RegistryAccess.fromRegistryOfRegistries(this.makeRegistryOfRegistries());
        }
        // Mark transition as complete (required for the next step)
        this.stage = newStage;
        // Transition all children while making sure, a registry set is always transitioned after all its parents
        List<DatagenRegistrySet> childrenLeft = new ArrayList<>(this.getDirectChildren());
        while (!childrenLeft.isEmpty()) {
            boolean deadCycle = true;
            Iterator<DatagenRegistrySet> itr = childrenLeft.iterator();
            while (itr.hasNext()) {
                DatagenRegistrySet set = itr.next();
                if (set.getDirectParents().stream().allMatch(parent -> parent.stage == newStage)) {
                    // All parents have transitioned
                    set.doTransition(newStage);
                    deadCycle = false;
                    itr.remove();
                }
            }
            if (deadCycle) throw new IllegalStateException("Dead cycle in state transition detected. This should not happen.");
        }
    }
    
    private boolean shouldBeFrozen(DatagenStage stage, ResourceKey<? extends Registry<?>> registryKey) {
        return switch (stage) {
            case REGISTRY_SETUP -> false;
            case EXTENSION_SETUP -> !DatagenSystem.extensionRegistries().contains(registryKey);
            case DATAGEN -> true;
        };
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Registry<? extends Registry<?>> makeRegistryOfRegistries() {
        WritableRegistry<? extends Registry<?>> rootRegistry = new MappedRegistry<>(ResourceKey.createRegistryKey(BuiltInRegistries.ROOT_REGISTRY_NAME), Lifecycle.stable());
        for (ResourceKey<? extends Registry<?>> key : this.rootAccess.registries().map(RegistryAccess.RegistryEntry::key).toList()) {
            ((WritableRegistry) rootRegistry).register(key, this.registry((ResourceKey) key), Lifecycle.stable());
        }
        return rootRegistry;
    }
    
    public void writeElements(PackTarget target, CachedOutput output) {
        if (this.isRoot()) {
            throw new IllegalStateException("The root registry set can't write elements.");
        }
        if (this.stage != DatagenStage.DATAGEN) {
            throw new IllegalStateException("Can't serialize registries during " + this.stage + " phase.");
        }
        for (DatagenRegistry<?> registry : this.registries.values()) {
            registry.writeOwnElements(target, output);
        }
    }
}

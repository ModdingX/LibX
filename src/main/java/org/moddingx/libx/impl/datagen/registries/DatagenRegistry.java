package org.moddingx.libx.impl.datagen.registries;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.*;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.TagKey;
import org.moddingx.libx.datagen.PackTarget;
import org.moddingx.libx.datapack.DatapackHelper;
import org.moddingx.libx.util.lazy.LazyValue;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class DatagenRegistry<T> extends MappedRegistry<T> {

    private final DatagenRegistrySet registrySet;
    private final Codec<T> codec;
    private final Lookup lookup;
    private boolean frozen;
    private boolean propagateNewElementsToChildren;
    
    public static <T> DatagenRegistry<T> createRoot(ResourceKey<? extends Registry<T>> registryKey, DatagenRegistrySet registrySet, Codec<T> codec, Registry<T> rootRegistry) {
        return new DatagenRegistry<>(registryKey, registrySet, codec, List.of(rootRegistry));
    }
    
    public static <T> DatagenRegistry<T> create(ResourceKey<? extends Registry<T>> registryKey, DatagenRegistrySet registrySet, Codec<T> codec, List<DatagenRegistry<T>> parents) {
        return new DatagenRegistry<>(registryKey, registrySet, codec, parents.stream().map(dr -> (Registry<T>) dr).toList());
    }
    
    private DatagenRegistry(ResourceKey<? extends Registry<T>> registryKey, DatagenRegistrySet registrySet, Codec<T> codec, List<Registry<T>> fillFrom) {
        super(registryKey, Lifecycle.stable());
        this.registrySet = registrySet;
        this.codec = codec;
        this.lookup = new Lookup();
        this.frozen = false;
        this.propagateNewElementsToChildren = true;
        // Load this registry with the elements from the parents
        // We need to merge them here
        Map<ResourceKey<T>, T> parentElements = new HashMap<>();
        for (Registry<T> parent : fillFrom) {
            for (Map.Entry<ResourceKey<T>, T> entry : parent.entrySet()) {
                ResourceKey<T> key = entry.getKey();
                T value = entry.getValue();
                if (parentElements.containsKey(key) && value != parentElements.get(key)) {
                    throw new IllegalStateException("Can't create registry set: Inherited two different values for key " + key + ": " + parentElements.get(key) + " and " + value);
                } else {
                    parentElements.put(key, value);
                }
            }
        }
        for (Map.Entry<ResourceKey<T>, T> entry : parentElements.entrySet()) {
            this.register(entry.getKey(), entry.getValue(), Lifecycle.stable());
        }
    }
    
    @Nonnull
    @Override
    public HolderOwner<T> holderOwner() {
        return this.lookup;
    }

    @Nonnull
    @Override
    public HolderLookup.RegistryLookup<T> asLookup() {
        return this.lookup;
    }

    @Nonnull
    @Override
    public Holder.Reference<T> registerMapping(int id, @Nonnull ResourceKey<T> key, @Nonnull T value, @Nonnull Lifecycle lifecycle) {
        Holder.Reference<T> holder = super.registerMapping(id, key, value, Lifecycle.stable());
        if (this.propagateNewElementsToChildren) {
            // Register to all children (can't keep ids consistent)
            Set<DatagenRegistry<T>> activeChildren = this.registrySet.collectActiveChildRegistries(this.key());
            for (DatagenRegistry<T> child : activeChildren) {
                if (child.containsKey(key)) {
                    throw new IllegalStateException("Can't add element to datagen registry: Already registered in child registry");
                }
            }
            for (DatagenRegistry<T> child : activeChildren) {
                child.registerOnlyThisRegistry(key, value, lifecycle);
            }
        }
        return holder;
    }

    @SuppressWarnings("UnusedReturnValue")
    private Holder.Reference<T> registerOnlyThisRegistry(ResourceKey<T> key, T value, Lifecycle lifecycle) {
        try {
            this.propagateNewElementsToChildren = false;
            return this.register(key, value, lifecycle);
        } finally {
            this.propagateNewElementsToChildren = true;
        }
    }

    @Nonnull
    @Override
    public Registry<T> freeze() {
        for (DatagenRegistrySet parent : this.registrySet.getDirectParents()) {
            if (parent.getDatagenRegistry(this.key(), false).stream().anyMatch(reg -> !reg.frozen)) {
                throw new IllegalStateException("Can't freeze datagen registry while its parents are still unfrozen.");
            }
        }
        this.frozen = true;
        return super.freeze();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void unfreeze() {
        throw new UnsupportedOperationException();
    }

    public void writeOwnElements(PackTarget target, CachedOutput output) {
        if (!this.frozen) throw new IllegalStateException("Can't serialize unfrozen registry: " + this.key());
        
        // Must use a lazy value as pack types which don't support datapack outputs also have registries
        // but can't query the datapack output path
        LazyValue<Path> outputPath = new LazyValue<>(() -> target.path(PackType.SERVER_DATA));
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, this.registrySet.registryAccess());
        
        List<DatagenRegistry<T>> parents = this.registrySet.getDirectParents().stream().flatMap(parent -> parent.getDatagenRegistry(this.key(), false).stream()).toList();
        for (Map.Entry<ResourceKey<T>, T> entry : this.entrySet()) {
            if (parents.stream().noneMatch(reg -> reg.containsKey(entry.getKey()))) {
                try {
                    JsonElement json = this.codec.encodeStart(ops, entry.getValue()).getOrThrow(false, msg -> {});
                    DataProvider.saveStable(output, json, outputPath.get().resolve(DatapackHelper.registryPath(entry.getKey())));
                } catch (Exception e) {
                    throw new RuntimeException("Failed to serialise element " + entry.getKey() + " in datagen registry", e);
                }
            }
        }
    }

    private class Lookup implements HolderLookup.RegistryLookup<T> {

        @Nonnull
        @Override
        public ResourceKey<? extends Registry<? extends T>> key() {
            return DatagenRegistry.this.key();
        }

        @Nonnull
        @Override
        public Lifecycle registryLifecycle() {
            return DatagenRegistry.this.registryLifecycle();
        }

        @Nonnull
        @Override
        public Stream<Holder.Reference<T>> listElements() {
            return DatagenRegistry.this.holders();
        }

        @Nonnull
        @Override
        public Stream<HolderSet.Named<T>> listTags() {
            return DatagenRegistry.this.getTags().map(Pair::getSecond);
        }

        @Nonnull
        @Override
        public Optional<Holder.Reference<T>> get(@Nonnull ResourceKey<T> key) {
            return DatagenRegistry.this.getHolder(key);
        }

        @Nonnull
        @Override
        public Optional<HolderSet.Named<T>> get(@Nonnull TagKey<T> key) {
            return DatagenRegistry.this.getTag(key);
        }

        @Override
        public boolean canSerializeIn(@Nonnull HolderOwner<T> owner) {
            return true;
        }
    }
}

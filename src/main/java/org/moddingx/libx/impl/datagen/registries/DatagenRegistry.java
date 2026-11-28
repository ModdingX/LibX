package org.moddingx.libx.impl.datagen.registries;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.WithConditions;
import org.moddingx.libx.datagen.PackTarget;
import org.moddingx.libx.datapack.DatapackHelper;
import org.moddingx.libx.util.lazy.LazyValue;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.*;

public class DatagenRegistry<T> extends MappedRegistry<T> {

    private final DatagenRegistrySet registrySet;
    private final Codec<T> codec;
    private final Map<ResourceKey<T>, List<ICondition>> conditions;
    private boolean frozen;
    private boolean propagateNewElementsToChildren;

    public static <T> DatagenRegistry<T> createRoot(ResourceKey<? extends Registry<T>> registryKey, DatagenRegistrySet registrySet, Codec<T> codec, Registry<T> rootRegistry) {
        return new DatagenRegistry<>(registryKey, registrySet, codec, List.of(rootRegistry));
    }

    public static <T> DatagenRegistry<T> create(ResourceKey<? extends Registry<T>> registryKey, DatagenRegistrySet registrySet, Codec<T> codec, List<DatagenRegistry<T>> parents) {
        return new DatagenRegistry<>(registryKey, registrySet, codec, parents.stream().map(dr -> (Registry<T>) dr).toList());
    }

    private DatagenRegistry(ResourceKey<? extends Registry<T>> registryKey, DatagenRegistrySet registrySet, Codec<T> codec, List<Registry<T>> fillFrom) {
        super(registryKey, Lifecycle.stable(), true);
        this.registrySet = registrySet;
        this.codec = codec;
        this.conditions = new HashMap<>();
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
            this.register(entry.getKey(), entry.getValue(), RegistrationInfo.BUILT_IN);
        }
    }

    @Nonnull
    @Override
    public Holder.Reference<T> register(int id, @Nonnull ResourceKey<T> key, @Nonnull T value, @Nonnull RegistrationInfo info) {
        if (this.unregisteredIntrusiveHolders != null && !this.unregisteredIntrusiveHolders.containsKey(value)) {
            // We allow intrusive holders, however this implies, every value must have an intrusive holder
            // which is not the case. Create one on the fly.
            this.createIntrusiveHolder(value);
        }
        Holder.Reference<T> holder = super.register(id, key, value, RegistrationInfo.BUILT_IN);
        if (this.propagateNewElementsToChildren) {
            // Register to all children (can't keep ids consistent)
            Set<DatagenRegistry<T>> activeChildren = this.registrySet.collectActiveChildRegistries(this.key());
            for (DatagenRegistry<T> child : activeChildren) {
                if (child.containsKey(key)) {
                    throw new IllegalStateException("Can't add element to datagen registry: Already registered in child registry");
                }
            }
            for (DatagenRegistry<T> child : activeChildren) {
                child.registerOnlyThisRegistry(key, value, info);
            }
        }
        this.registrySet.trackHolderTarget(holder, this.key());
        return holder;
    }

    @Nonnull
    @Override
    public Holder.Reference<T> createIntrusiveHolder(@Nonnull T value) {
        Holder.Reference<T> holder = this.getResourceKey(value).flatMap(this::get).orElse(null);
        if (holder == null) holder = super.createIntrusiveHolder(value);
        this.registrySet.trackHolderTarget(holder, this.key());
        return holder;
    }

    @SuppressWarnings("UnusedReturnValue")
    private Holder.Reference<T> registerOnlyThisRegistry(ResourceKey<T> key, T value, RegistrationInfo info) {
        try {
            this.propagateNewElementsToChildren = false;
            return this.register(key, value, info);
        } finally {
            this.propagateNewElementsToChildren = true;
        }
    }

    /**
     * Sets the loading conditions for an element, that are written along with the element.
     */
    public void setConditions(ResourceKey<T> key, List<ICondition> conditions) {
        if (this.frozen) {
            throw new IllegalStateException("Can't set conditions on frozen registry: " + this.key());
        }

        if (conditions.isEmpty()) {
            this.conditions.remove(key);
        } else {
            this.conditions.put(key, List.copyOf(conditions));
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
        Registry<T> result = super.freeze();
        this.frozen = true;
        return result;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void unfreeze(boolean clearTags) {
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
                    List<ICondition> elementConditions = this.conditions.getOrDefault(entry.getKey(), List.of());
                    JsonElement json = elementConditions.isEmpty()
                            ? this.codec.encodeStart(ops, entry.getValue()).getOrThrow(RuntimeException::new)
                            : ConditionalOps.createConditionalCodecWithConditions(this.codec).encodeStart(ops, Optional.of(new WithConditions<>(elementConditions, entry.getValue()))).getOrThrow(RuntimeException::new);
                    DataProvider.saveStable(output, json, outputPath.get().resolve(DatapackHelper.registryPath(entry.getKey())));
                } catch (Exception e) {
                    throw new RuntimeException("Failed to serialise element " + entry.getKey() + " in datagen registry", e);
                }
            }
        }
    }
}

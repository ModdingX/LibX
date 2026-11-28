package org.moddingx.libx.impl.datagen.registries;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.*;
import net.minecraft.core.registries.EmptyTagLookupWrapper;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.moddingx.libx.datagen.RegistrySet;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A {@link BootstrapContext} that registers elements into the writable registries of a {@link RegistrySet}.
 * This allows using vanilla data providers that are based on bootstrap contexts (like the recipe provider)
 * with the LibX datagen system.
 */
public class DatagenBootstrapContext<T> implements BootstrapContext<T> {

    private final RegistrySet registries;
    private final ResourceKey<? extends Registry<T>> registryKey;
    private final MappedRegistry<T> pending;

    public DatagenBootstrapContext(RegistrySet registries, ResourceKey<? extends Registry<T>> registryKey) {
        this.registries = registries;
        this.registryKey = registryKey;
        this.pending = new MappedRegistry<>(registryKey, Lifecycle.stable());
    }

    @Nonnull
    @Override
    public Holder.Reference<T> register(@Nonnull ResourceKey<T> key, @Nonnull T value) {
        return this.register(key, value, new ICondition[0]);
    }

    @Nonnull
    @Override
    public Holder.Reference<T> register(@Nonnull ResourceKey<T> key, @Nonnull T value, @Nonnull ICondition... conditions) {
        WritableRegistry<T> registry = this.registries.writableRegistry(this.registryKey);
        Holder.Reference<T> holder = registry.register(key, value, RegistrationInfo.BUILT_IN);
        if (conditions.length != 0) {
            if (!(registry instanceof DatagenRegistry<T> datagenRegistry)) {
                throw new IllegalStateException("Can't add conditions to registry " + this.registryKey.identifier());
            }
            datagenRegistry.setConditions(key, List.of(conditions));
        }
        // Bind holders that were requested before registration
        this.pending.register(key, value, RegistrationInfo.BUILT_IN);
        return holder;
    }

    @Nonnull
    @Override
    public <S> HolderGetter<S> lookup(@Nonnull ResourceKey<? extends Registry<? extends S>> key) {
        HolderGetter<S> lookup = EmptyTagLookupWrapper.wrap(this.registries.registry((ResourceKey<? extends Registry<S>>) key));
        if (!Objects.equals(key, this.registryKey)) return lookup;
        HolderGetter<S> pendingLookup = (HolderGetter<S>) this.pending.createRegistrationLookup();
        return new HolderGetter<>() {

            @Nonnull
            @Override
            public Optional<Holder.Reference<S>> get(@Nonnull ResourceKey<S> elementKey) {
                Optional<Holder.Reference<S>> holder = lookup.get(elementKey);
                return holder.isPresent() ? holder : pendingLookup.get(elementKey);
            }

            @Nonnull
            @Override
            public Optional<HolderSet.Named<S>> get(@Nonnull TagKey<S> tagKey) {
                return lookup.get(tagKey);
            }
        };
    }

    @Nonnull
    @Override
    @Deprecated
    public <S> Stream<Holder.Reference<S>> listContextElements(@Nonnull ResourceKey<? extends Registry<? extends S>> key) {
        return this.registries.registry((ResourceKey<? extends Registry<S>>) key).listElements();
    }
}

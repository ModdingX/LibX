package io.github.noeppi_noeppi.libx.impl.registration;

import io.github.noeppi_noeppi.libx.registration.MultiRegisterable;
import io.github.noeppi_noeppi.libx.registration.Registerable;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import javax.annotation.Nullable;

public class SingleEntryCollector implements Registerable.EntryCollector {

    private final RegistrationDispatcher dispatcher;
    private final String baseId;

    public SingleEntryCollector(RegistrationDispatcher dispatcher, String baseId) {
        this.dispatcher = dispatcher;
        this.baseId = baseId;
    }
    
    @Override
    public <T> void register(@Nullable ResourceKey<? extends Registry<T>> registry, T value) {
        this.dispatcher.register(registry, this.baseId, value);
    }

    @Override
    public <T> void registerNamed(@Nullable ResourceKey<? extends Registry<T>> registry, String name, T value) {
        this.dispatcher.register(registry, this.baseId + "_" + name, value);
    }

    @Override
    public <T> void registerMulti(@Nullable ResourceKey<? extends Registry<T>> registry, MultiRegisterable<T> value) {
        this.dispatcher.registerMulti(registry, this.baseId, value);
    }

    @Override
    public <T> void registerMultiNamed(@Nullable ResourceKey<? extends Registry<T>> registry, String name, MultiRegisterable<T> value) {
        this.dispatcher.registerMulti(registry, this.baseId + "_" + name, value);
    }

    @Override
    public <T> Holder<T> createHolder(@Nullable ResourceKey<? extends Registry<T>> registry, T value) {
        return this.dispatcher.register(registry, this.baseId, value).get();
    }

    @Override
    public <T> Holder<T> createNamedHolder(@Nullable ResourceKey<? extends Registry<T>> registry, String name, T value) {
        return this.dispatcher.register(registry, this.baseId + "_" + name, value).get();
    }
}

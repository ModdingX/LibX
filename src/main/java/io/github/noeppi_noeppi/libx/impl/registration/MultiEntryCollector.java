package io.github.noeppi_noeppi.libx.impl.registration;

import io.github.noeppi_noeppi.libx.registration.MultiRegisterable;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import javax.annotation.Nullable;

public class MultiEntryCollector<T> implements MultiRegisterable.EntryCollector<T> {

    @Nullable
    private final ResourceKey<? extends Registry<T>> registryKey;
    private final RegistrationDispatcher dispatcher;
    private final String baseId;

    public MultiEntryCollector(RegistrationDispatcher dispatcher, @Nullable ResourceKey<? extends Registry<T>> registryKey, String baseId) {
        this.dispatcher = dispatcher;
        this.registryKey = registryKey;
        this.baseId = baseId;
    }

    @Override
    public void register(T value) {
        this.dispatcher.register(this.registryKey, this.baseId, value);
    }

    @Override
    public void registerNamed(String name, T value) {
        this.dispatcher.register(this.registryKey, this.baseId + "_" + name, value);
    }

    @Override
    public void registerMulti(MultiRegisterable<T> value) {
        this.dispatcher.registerMulti(this.registryKey, this.baseId, value);
    }

    @Override
    public void registerMultiNamed(String name, MultiRegisterable<T> value) {
        this.dispatcher.registerMulti(this.registryKey, this.baseId + "_" + name, value);
    }

    @Override
    public Holder<T> createHolder(T value) {
        return this.dispatcher.register(this.registryKey, this.baseId, value).get();
    }

    @Override
    public Holder<T> createNamedHolder(String name, T value) {
        return this.dispatcher.register(this.registryKey, this.baseId + "_" + name, value).get();
    }
}

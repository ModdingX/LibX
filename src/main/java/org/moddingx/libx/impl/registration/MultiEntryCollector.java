package org.moddingx.libx.impl.registration;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.moddingx.libx.registration.MultiRegisterable;

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
}

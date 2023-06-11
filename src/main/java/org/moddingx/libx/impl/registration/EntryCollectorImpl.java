package org.moddingx.libx.impl.registration;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.moddingx.libx.registration.Registerable;

import javax.annotation.Nullable;

public class EntryCollectorImpl implements Registerable.EntryCollector {

    private final RegistrationDispatcher dispatcher;
    private final String baseId;

    public EntryCollectorImpl(RegistrationDispatcher dispatcher, String baseId) {
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
}

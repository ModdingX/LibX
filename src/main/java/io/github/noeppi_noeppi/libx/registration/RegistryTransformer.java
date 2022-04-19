package io.github.noeppi_noeppi.libx.registration;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import javax.annotation.Nullable;

public interface RegistryTransformer {
    
    default void transform(RegistrationContext ctx, Object value, Registerable.EntryCollector collector) {
        
    }
    
    default <T> void transformMulti(RegistrationContext ctx, @Nullable ResourceKey<? extends Registry<T>> registry, Object value, MultiRegisterable.EntryCollector<T> collector) {
        
    }
}

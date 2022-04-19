package io.github.noeppi_noeppi.libx.registration;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import javax.annotation.Nullable;

public interface RegistryCondition {
    
    default boolean shouldRegister(RegistrationContext ctx, Object value) {
        return true;
    }
    
    default <T> boolean shouldRegisterMulti(RegistrationContext ctx, @Nullable ResourceKey<? extends Registry<T>> registry, MultiRegisterable<T> value) {
        return true;
    }
}

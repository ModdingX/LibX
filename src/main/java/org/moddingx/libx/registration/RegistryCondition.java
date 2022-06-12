package org.moddingx.libx.registration;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import javax.annotation.Nullable;

/**
 * A registry condition is used by the LibX registration system to prevent things passed to the
 * register methods from being registered.
 *
 * @see RegistrationBuilder
 */
public interface RegistryCondition {

    /**
     * Tests whether a given object should be registered. In order for an object to be registered, all
     * conditions must return {@code true}.
     */
    default boolean shouldRegister(RegistrationContext ctx, Object value) {
        return true;
    }
    
    /**
     * Tests whether a given {@link MultiRegisterable} should be registered. In order for an object to
     * be registered, all conditions must return {@code true}.
     */
    default <T> boolean shouldRegisterMulti(RegistrationContext ctx, @Nullable ResourceKey<? extends Registry<T>> registry, MultiRegisterable<T> value) {
        return true;
    }
}

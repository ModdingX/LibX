package org.moddingx.libx.registration;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.moddingx.libx.annotation.meta.RemoveIn;

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
        // TODO 1.20: Method should no longer be a default method.
        return true;
    }
    
    /**
     * Tests whether a given {@link MultiRegisterable} should be registered. In order for an object to
     * be registered, all conditions must return {@code true}.
     *
     * @deprecated {@link MultiRegisterable} is deprecated. See there for more information
     */
    @Deprecated(forRemoval = true)
    @RemoveIn(minecraft = "1.20")
    default <T> boolean shouldRegisterMulti(RegistrationContext ctx, @Nullable ResourceKey<? extends Registry<T>> registry, MultiRegisterable<T> value) {
        return true;
    }
}

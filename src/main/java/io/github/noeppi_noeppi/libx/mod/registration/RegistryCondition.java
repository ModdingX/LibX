package io.github.noeppi_noeppi.libx.mod.registration;

import io.github.noeppi_noeppi.libx.annotation.meta.RemoveIn;
import net.minecraft.resources.ResourceLocation;

/**
 * A registry condition is used by the LibX registration system to prevent things
 * passed to {@link ModXRegistration#register(String, Object)} from being registered.
 * For more information see {@link ModXRegistration#initRegistration(RegistrationBuilder)}
 * 
 * @see ModXRegistration#initRegistration(RegistrationBuilder)
 *
 * @deprecated See https://gist.github.com/noeppi-noeppi/9de9b6af950ee02f2dee611742fe2d6d
 */
@Deprecated(forRemoval = true)
@RemoveIn(minecraft = "1.19")
public interface RegistryCondition {

    /**
     * Gets whether an object should be registered into the LibX registration system. All
     * conditions must return true for an object to be registered.
     */
    boolean shouldRegister(ResourceLocation id, Object object);
}

package io.github.noeppi_noeppi.libx.mod.registration;

import net.minecraft.resources.ResourceLocation;

/**
 * A registry condition is used by the LibX registration system to prevent things
 * passed to {@link ModXRegistration#register(String, Object)} from being registered.
 * For more information see {@link ModXRegistration#initRegistration(RegistrationBuilder)}
 * 
 * @see ModXRegistration#initRegistration(RegistrationBuilder)
 */
public interface RegistryCondition {

    /**
     * Gets whether an object should be registered into the LibX registration system. All
     * conditions must return true for an object to be registered.
     */
    boolean shouldRegister(ResourceLocation id, Object object);
}

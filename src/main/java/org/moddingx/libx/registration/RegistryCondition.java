package org.moddingx.libx.registration;

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
    boolean shouldRegister(RegistrationContext ctx, Object value);
}

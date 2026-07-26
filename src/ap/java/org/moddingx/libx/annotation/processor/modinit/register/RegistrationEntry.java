package org.moddingx.libx.annotation.processor.modinit.register;

import javax.annotation.Nullable;

/**
 * @param needsId Whether the registry id needs to be injected into the {@code Properties} object that is passed to
 *                the constructor of the field value. This is only required for items and blocks.
 */
public record RegistrationEntry(@Nullable String registryFqn, String name, String fieldClassFqn, String fieldName, boolean needsId) {

}

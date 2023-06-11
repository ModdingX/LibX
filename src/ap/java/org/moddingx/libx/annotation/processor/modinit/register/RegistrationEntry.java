package org.moddingx.libx.annotation.processor.modinit.register;

import javax.annotation.Nullable;

public record RegistrationEntry(@Nullable String registryFqn, String name, String fieldClassFqn, String fieldName) {
    
}

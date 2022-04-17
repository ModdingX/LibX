package io.github.noeppi_noeppi.libx.registration;

public interface RegistryCondition {
    
    boolean shouldRegister(RegistrationContext ctx, Object value);
}

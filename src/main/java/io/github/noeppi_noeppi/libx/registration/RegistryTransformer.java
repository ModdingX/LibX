package io.github.noeppi_noeppi.libx.registration;

public interface RegistryTransformer {
    
    void buildAdditionalRegisters(RegistrationContext ctx, Object value, Registerable.EntryCollector collector);
}

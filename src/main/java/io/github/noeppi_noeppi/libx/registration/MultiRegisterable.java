package io.github.noeppi_noeppi.libx.registration;

import net.minecraft.core.Holder;

// Can't be registered into any registry
// doing so will fail
// passed registry is used for children
public interface MultiRegisterable<T> {
    
    // TODO strict super
    default void buildAdditionalRegisters(RegistrationContext ctx, EntryCollector<T> builder) {
        
    }
    
    interface EntryCollector<T> {
        
        void register(T value);
        void registerNamed(String name, T value);
        
        void registerMulti(MultiRegisterable<T> value);
        void registerMultiNamed(String name, MultiRegisterable<T> value);
        
        Holder<T> createHolder(T value);
        Holder<T> createNamedHolder(String name, T value);
    }
}

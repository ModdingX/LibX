package io.github.noeppi_noeppi.libx.registration;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import javax.annotation.Nullable;

/**
 * A registry transformer is used by the LibX registration system to register additional
 * things dynamically based on other objects, that have been passed to the system.
 *
 * @see RegistrationBuilder
 */
public interface RegistryTransformer {

    /**
     * Registers additional objects to the given object.
     * 
     * @see Registerable#buildAdditionalRegisters(RegistrationContext, Registerable.EntryCollector)
     */
    default void transform(RegistrationContext ctx, Object value, Registerable.EntryCollector collector) {
        
    }

    /**
     * Registers additional objects to the given object.
     *
     * @see MultiRegisterable#buildAdditionalRegisters(RegistrationContext, MultiRegisterable.EntryCollector)
     */
    default <T> void transformMulti(RegistrationContext ctx, @Nullable ResourceKey<? extends Registry<T>> registry, Object value, MultiRegisterable.EntryCollector<T> collector) {
        
    }
}

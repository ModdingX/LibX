package org.moddingx.libx.registration;

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
     * @see Registerable#registerAdditional(RegistrationContext, Registerable.EntryCollector)
     */
    void transform(RegistrationContext ctx, Object value, Registerable.EntryCollector builder);
}

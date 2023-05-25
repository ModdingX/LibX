package org.moddingx.libx.registration;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.moddingx.libx.annotation.meta.RemoveIn;

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
     * @see Registerable#registerAdditional(RegistrationContext, Registerable.EntryCollector)
     */
    default void transform(RegistrationContext ctx, Object value, Registerable.EntryCollector builder) {
        // TODO 1.20: Method should no longer be a default method.
    }

    /**
     * Registers additional objects to the given object.
     *
     * @see MultiRegisterable#registerAdditional(RegistrationContext, MultiRegisterable.EntryCollector)
     *
     * @deprecated {@link MultiRegisterable} is deprecated. See there for more information
     */
    @Deprecated(forRemoval = true)
    @RemoveIn(minecraft = "1.20")
    default <T> void transformMulti(RegistrationContext ctx, @Nullable ResourceKey<? extends Registry<T>> registry, Object value, MultiRegisterable.EntryCollector<T> builder) {
        
    }
}

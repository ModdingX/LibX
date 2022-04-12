package io.github.noeppi_noeppi.libx.mod.registration;

import io.github.noeppi_noeppi.libx.annotation.meta.RemoveIn;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * A registry transformer is used by the LibX registration system to register additional
 * things dynamically based on other objects, that have been passed to the system. For
 * more information see {@link ModXRegistration#initRegistration(RegistrationBuilder)}
 * 
 * @see ModXRegistration#initRegistration(RegistrationBuilder)
 *
 * @deprecated See https://gist.github.com/noeppi-noeppi/9de9b6af950ee02f2dee611742fe2d6d
 */
@Deprecated(forRemoval = true)
@RemoveIn(minecraft = "1.19")
public interface RegistryTransformer {

    /**
     * Gets an object that should be passed to the system in addition to the given object.
     * This method will et called for that {@link Registerable} and all things it registers again, so
     * be careful to not create infinite loops.
     * 
     * <b>IMPORTANT: The transformers created by LibX may also consume the original object
     * so you might not always get everything you registered here. However this feature is
     * not available for custom transformers.</b>
     * 
     * The new object is registered with the same id as the old one. If this is not wanted,
     * wrap it into a {@link Registerable} via {@link Registerable#getNamedAdditionalRegisters(ResourceLocation)}.
     */
    @Nullable
    Object getAdditional(ResourceLocation id, Object object);
}

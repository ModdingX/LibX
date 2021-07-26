package io.github.noeppi_noeppi.libx.annotation.codec;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;

import java.lang.annotation.*;

/**
 * The registry lookup codec field type will try to get a  {@link Registry registry} from
 * the {@link RegistryAccess}. With this annotation you can customise which registry should
 * be retrieved by setting the resource key for the registry to use.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Documented
public @interface Lookup {

    /**
     * The path of the registry id. If left empty, it'll be auto-detected.
     */
    String value() default "";
    
    /**
     * The namespace of the registry id. Defaults to {@code minecraft}.
     */
    String namespace() default "minecraft";
}

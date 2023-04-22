package org.moddingx.libx.datagen.provider.sandbox;

import java.lang.annotation.*;

/**
 * Can be applied to fields in subclasses of {@link SandBoxProviderBase} to register an element by a different id.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Id {

    /**
     * The namespace used to register the element. Defaults to the current mods namespace.
     */
    String namespace() default "";
    
    /**
     * The path used to register the element.
     */
    String value();
}

package io.github.noeppi_noeppi.libx.config;

import java.lang.annotation.*;

/**
 * This annotation is added to a field that serves as a config value. See
 * {@link ConfigManager} for more info.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Config {

    /**
     * The comment for this config value.
     */
    String[] value() default {};

    /**
     * The resource location of a {@link ValueMapper} to convert this value.
     * Leave it out to automatically detect a matching mapper fromn the builtin
     * ones. Auto detection is not possible with custom mappers. 
     */
    String mapper() default "";
    
    /**
     * This class is passed to the value mapper to allow for generics. Examples
     * where this is used are lists and maps.
     */
    Class<?> elementType() default void.class;
}

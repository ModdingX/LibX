package org.moddingx.libx.config.validator;

import java.lang.annotation.*;

/**
 * Config validator annotation that checks whether a short is in a range of allowed values.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface ShortRange {

    /**
     * The lower bound (inclusive)
     */
    short min() default Short.MIN_VALUE;
    
    /**
     * The upper bound (inclusive)
     */
    short max() default Short.MAX_VALUE;
}

package org.moddingx.libx.config.validator;

import java.lang.annotation.*;

/**
 * Config validator annotation that checks whether a float is in a range of allowed values.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface FloatRange {

    /**
     * The lower bound (inclusive)
     */
    float min() default Float.NEGATIVE_INFINITY;
    
    /**
     * The upper bound (inclusive)
     */
    float max() default Float.POSITIVE_INFINITY;
}

package org.moddingx.libx.config.validate;

import java.lang.annotation.*;

/**
 * Config validator annotation that checks whether an integer is in a range of allowed values.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Documented
public @interface IntRange {

    /**
     * The lower bound (inclusive)
     */
    int min() default Integer.MIN_VALUE;
    
    /**
     * The upper bound (inclusive)
     */
    int max() default Integer.MAX_VALUE;
}

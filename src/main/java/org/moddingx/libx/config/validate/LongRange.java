package org.moddingx.libx.config.validate;

import java.lang.annotation.*;

/**
 * Config validator annotation that checks whether a long is in a range of allowed values.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface LongRange {

    /**
     * The lower bound (inclusive)
     */
    long min() default Long.MIN_VALUE;
    
    /**
     * The upper bound (inclusive)
     */
    long max() default Long.MAX_VALUE;
}

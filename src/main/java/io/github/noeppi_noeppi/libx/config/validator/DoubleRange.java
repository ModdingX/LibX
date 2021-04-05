package io.github.noeppi_noeppi.libx.config.validator;

import java.lang.annotation.*;

/**
 * Config validator that checks whether a double is in a range of allowed values.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface DoubleRange {

    /**
     * The lower bound (inclusive)
     */
    double min() default Double.NEGATIVE_INFINITY;
    
    /**
     * The upper bound (inclusive)
     */
    double max() default Double.POSITIVE_INFINITY;
}

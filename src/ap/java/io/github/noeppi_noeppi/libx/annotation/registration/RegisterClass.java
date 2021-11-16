package io.github.noeppi_noeppi.libx.annotation.registration;

import java.lang.annotation.*;

/**
 * Can be applied to a class to register all {@code public static final} fields to a {@code ModXRegistration}.
 * 
 * To ignore a field add {@link NoReg @NoReg} to it.
 * 
 * By default the name from the field is taken and translated to snake case. To explicitly set a name use
 * {@link RegName @RegName}.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface RegisterClass {

    /**
     * Higher priorities are registered first.
     */
    int priority() default 0;

    /**
     * All elements from this class are prefixed with a special prefix. This way you can keep field names simple
     * and avoid duplicates.
     */
    String prefix() default "";
}

package org.moddingx.libx.annotation.registration;

import org.moddingx.libx.annotation.meta.RemoveIn;

import java.lang.annotation.*;

/**
 * Can be applied to a class to register all {@code public static final} fields to a {@code ModXRegistration}.
 * 
 * To ignore a field add {@link NoReg @NoReg} to it.
 * 
 * By default the name from the field is taken and translated to snake case. To explicitly set a name use
 * {@link RegName @RegName}.
 *
 * @deprecated See https://gist.github.com/noeppi-noeppi/9de9b6af950ee02f2dee611742fe2d6d
 */
@Deprecated(forRemoval = true)
@RemoveIn(minecraft = "1.19")
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

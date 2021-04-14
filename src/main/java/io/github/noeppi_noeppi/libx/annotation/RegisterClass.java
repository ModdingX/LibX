package io.github.noeppi_noeppi.libx.annotation;

import io.github.noeppi_noeppi.libx.mod.registration.ModXRegistration;

import java.lang.annotation.*;

/**
 * <b>This can only be used in LibX is added as an annotation processor!</b>
 * Apply to a class to register all public static final fields to a {@link ModXRegistration}
 * To ignore a field add @{@link NoReg} to it.
 * By default the name from the field is taken and translated to snake case (each uppercase letter is replaced by an
 * underscore and the lowercase letter.) To explicitly set a name use @{@link RegName}
 * This will generate an additional class in the same package as yoiu registration class that ends with `$Registrate`
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

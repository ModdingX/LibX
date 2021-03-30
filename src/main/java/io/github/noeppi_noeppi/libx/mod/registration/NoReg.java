package io.github.noeppi_noeppi.libx.mod.registration;

import java.lang.annotation.*;

/**
 * Exclude a field from automatic registration.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@Documented
public @interface NoReg {

}

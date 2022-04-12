package io.github.noeppi_noeppi.libx.annotation.registration;

import io.github.noeppi_noeppi.libx.annotation.meta.RemoveIn;

import java.lang.annotation.*;

/**
 * Exclude a field from automatic registration.
 *
 * @deprecated See https://gist.github.com/noeppi-noeppi/9de9b6af950ee02f2dee611742fe2d6d
 */
@Deprecated(forRemoval = true)
@RemoveIn(minecraft = "1.19")
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@Documented
public @interface NoReg {

}

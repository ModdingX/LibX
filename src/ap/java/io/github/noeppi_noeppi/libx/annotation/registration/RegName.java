package io.github.noeppi_noeppi.libx.annotation.registration;

import java.lang.annotation.*;

/**
 * Set a custom name for automatic registration.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@Documented
public @interface RegName {

    String value();
}

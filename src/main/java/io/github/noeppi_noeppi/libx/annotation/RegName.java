package io.github.noeppi_noeppi.libx.annotation;

import java.lang.annotation.*;

/**
 * Set custom name for registration
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@Documented
public @interface RegName {

    String value();
}

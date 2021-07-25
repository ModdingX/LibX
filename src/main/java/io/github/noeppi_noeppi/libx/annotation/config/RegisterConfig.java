package io.github.noeppi_noeppi.libx.annotation.config;

import java.lang.annotation.*;

/**
 * Automatically registers a LibX config.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface RegisterConfig {

    String value() default "config";
    boolean client() default false;
}

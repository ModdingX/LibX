package io.github.noeppi_noeppi.libx.annotation;

/**
 * Automatically registers a LibX config.
 */
public @interface RegisterConfig {

    String value() default "config";
    boolean client() default false;
}

package io.github.noeppi_noeppi.libx.annotation.config;

import java.lang.annotation.*;

/**
 * Automatically registers a value mapper for the LibX config system. An annotated class
 * must define a public no-arg constructor.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface RegisterMapper {

}

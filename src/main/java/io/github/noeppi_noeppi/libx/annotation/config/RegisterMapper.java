package io.github.noeppi_noeppi.libx.annotation.config;

import io.github.noeppi_noeppi.libx.config.GenericValueMapper;
import io.github.noeppi_noeppi.libx.config.ValueMapper;

import java.lang.annotation.*;

/**
 * Automatically registers a value mapper for the LibX config system. An annotated class
 * must define a public no-arg constructor and implement either {@link ValueMapper} or
 * {@link GenericValueMapper}.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface RegisterMapper {

}

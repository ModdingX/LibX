package org.moddingx.libx.annotation.config;

import java.lang.annotation.*;

/**
 * Automatically registers a value mapper for the LibX config system. An annotated class
 * must define a public no-arg constructor.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface RegisterMapper {

    /**
     * The mod id of the mod which needs to be loaded to register this mapper.
     */
    String requiresMod() default "";
}

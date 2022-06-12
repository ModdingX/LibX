package org.moddingx.libx.annotation.config;

import java.lang.annotation.*;

/**
 * Automatically registers a class as a LibX config.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface RegisterConfig {

    /**
     * The name to use for the config.
     */
    String value() default "config";

    /**
     * Whether the config is a client config.
     */
    boolean client() default false;

    /**
     * The mod id of the mod which needs to be loaded to generate this config file.
     */
    String requiresMod() default "";
}

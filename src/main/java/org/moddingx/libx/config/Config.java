package org.moddingx.libx.config;

import java.lang.annotation.*;

/**
 * This annotation is added to a field that serves as a config value. See
 * {@link ConfigManager} for more info.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Config {

    /**
     * The comment for this config value.
     */
    String[] value() default {};
}

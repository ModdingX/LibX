package io.github.noeppi_noeppi.libx.config;

import java.lang.annotation.*;

/**
 * This annotation can be added to a static class inside a config class to add a comment to a sub group.
 * However the annotation is not required and may be omitted. See {@link ConfigManager} for more info.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Group {

    /**
     * The comment for this config group.
     */
    String[] value() default {};
}

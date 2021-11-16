package io.github.noeppi_noeppi.libx.annotation.meta;

import java.lang.annotation.*;

/**
 * Elements annotated with {@code Experimental} are subject to change. Use them at your own
 * risk. These should never be included in releases.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target(value = {
        ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.METHOD,
        ElementType.PACKAGE, ElementType.MODULE, ElementType.PARAMETER, ElementType.TYPE
})
public @interface Experimental {
    
}

package org.moddingx.libx.annotation.meta;

import java.lang.annotation.*;

/**
 * Marks the version in which a feature will be removed.
 * At least one of the parameters must be defined.
 * An element that is annotated with {@code @RemoveIn} must be annotated
 * with {@link Deprecated#forRemoval() @Deprecated(forRemoval = true)}.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target(value = {
        ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.METHOD,
        ElementType.PACKAGE, ElementType.MODULE, ElementType.PARAMETER, ElementType.TYPE
})
public @interface RemoveIn {

    /**
     * The minecraft version in which the feature should be removed.
     */
    String minecraft() default "";
    
    /**
     * The mod version in which the feature should be removed.
     */
    String mod() default "";
}

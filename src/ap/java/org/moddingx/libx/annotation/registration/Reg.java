package org.moddingx.libx.annotation.registration;

import java.lang.annotation.*;

/**
 * Container annotation. may not be used on any element.
 * 
 * Contains annotations to modify fields in a class annotated with {@link RegisterClass}.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({})
public @interface Reg {
    
    /**
     * Marks a field as excluded from registration.
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    @Documented
    @interface Exclude {

    }

    /**
     * Explicitly sets a name for a field that is registered.
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    @Documented
    @interface Name {
        
        String value();
    }
}

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
     * Marks a field as excluded from registration. The field is skipped entirely: LibX will neither register
     * its value nor clean up any registry state that creating the value may have produced.
     *
     * If the value of an excluded field is a registry object (an item, a block, a fluid, an entity type, ...)
     * that is still constructed, you must register it yourself, for example through
     * {@code ModXRegistration.register}. Constructing such an object without ever registering it leaves dangling state
     * behind in the game registries. An {@code Item} for example registers a data component initializer
     * in its constructor, so an item that is never registered will crash world load with
     * {@code IllegalStateException: Missing element ResourceKey[minecraft:item / ...]}.
     *
     * Therefore this annotation is meant for fields that don't hold registry objects at all, or for values
     * that you register on your own.
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

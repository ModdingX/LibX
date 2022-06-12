package org.moddingx.libx.annotation.meta;

import java.lang.annotation.*;

/**
 * Marker annotation for abstract methods. If an abstract method has this annotation,
 * each method that overrides the annotated method must be annotated with
 * {@link javax.annotation.OverridingMethodsMustInvokeSuper}.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SuperChainRequired {
    
}

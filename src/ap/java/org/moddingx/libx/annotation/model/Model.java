package org.moddingx.libx.annotation.model;

import java.lang.annotation.*;

/**
 * Can be attached to public static non-final baked model fields. The model will
 * then be put into those fields.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@Documented
public @interface Model {

    /**
     * The namespace of the model. Defaults to the modid inferred for this element.
     */
    String namespace() default "";

    /**
     * The path of the model.
     */
    String value();
}

package io.github.noeppi_noeppi.libx.annotation;

import java.lang.annotation.*;

/**
 * Can be attached to public static non-final fields with the type IBakedModel. The model will then be
 * put into those fields.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@Documented
public @interface Model {

    /**
     * The namespace of the model .Deaults to the modid inferred for this element. See {@link ForMod}.
     */
    String namespace() default "";

    /**
     * The path of the model.
     */
    String value();
}

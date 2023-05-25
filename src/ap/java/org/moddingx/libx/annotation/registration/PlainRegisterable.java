package org.moddingx.libx.annotation.registration;

import java.lang.annotation.*;

/**
 * This annotation can be added to a class to instruct ModInit to always register instances of that class without
 * a registry and don't check the element type. Note that this annotation is disallowed on interfaces. <b>Also note
 * that it's insufficient that the value assigned to a field has this annotation, it must be present on the fields
 * type.</b> Suppose a class {@code A} with {@code @PlainRegisterable}. The following field declaration will work:
 * 
 * <pre>
 * <code>
 * public static final A value = new A();
 * </code>
 * </pre>
 * 
 * while this field declaration won't:
 * 
 * <pre>
 * <code>
 * public static final {@link Object} value = new A();
 * </code>
 * </pre>
 * 
 * as in this case the field type is {@link Object} which is not annotated with {@code @PlainRegisterable}.
 */
@Retention(RetentionPolicy.CLASS) // Must be CLASS not SOURCE as the AP needs this in libraries
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface PlainRegisterable {
    
}

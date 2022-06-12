package org.moddingx.libx.annotation.codec;

import java.lang.annotation.*;

/**
 * This is used to create very simple Codecs for classes. It will always create
 * record codecs that fill values. Default values are not directly supported but
 * can be easily added by creating custom smaller sub-codecs.
 * 
 * Can be added to a {@link Record record} definition or exactly one constructor
 * in a class. The Codec will then be generated based on the parameters of that
 * record or constructor.
 * 
 * To retrieve the codec, use the {@code Codecs} class.
 * 
 * For each parameter, there must either be a public field with the same name,
 * a method with no arguments and the name of the parameter, or a java bean
 * styled getter method for the parameter name. This will then be used by the
 * codec for serialisation.
 * 
 * For each parameter, ModInit attempts to generate a matching codec field.
 * There are multiple types of codec fields. Each of them has an annotation that
 * can be applied to disable auto-detection and force a specific type or to help
 * ModInit detect the type correctly.
 * 
 * Currently the following field types are supported:
 * 
 * <li>
 *     <ul>{@link Param Parameter Fields}. These are the default for everything
 *     that is not covered by a different type.</ul>
 * </li>
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR})
@Documented
public @interface PrimaryConstructor {
    
}

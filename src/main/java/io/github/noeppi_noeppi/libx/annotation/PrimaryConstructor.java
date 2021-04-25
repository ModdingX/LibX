package io.github.noeppi_noeppi.libx.annotation;

import java.lang.annotation.*;

/**
 * This is used to create very simple Codecs for classes. It will always create
 * {@code RecordBuilderCodec} s that fill values. Default values are not directly
 * supported but can be easily added by creating custom smaller sub-codecs.
 * 
 * Can be added to exactly one public constructor of a class. This class will
 * have a codec generated based on the parameters. The parameters can be
 * customized with {@link Param}.
 * 
 * For each parameter, there must either be a public field with the same name,
 * a method with no arguments and the name of the parameter, or a java bean
 * styled getter method for the parameter name. This will then be used by the
 * codec for serialisation.
 * 
 * Also the field must either have a type where DataFixerUpper provides a codec,
 * the type of that parameter must have a public static field named {@code CODEC}
 * or a value must have been set explicitly by a {@link Param} annotation.
 * 
 * You can also use Lists of any of the supported codec types.
 * 
 * To get your codec, use {@link Codecs#get(Class, Class)}.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.CONSTRUCTOR)
@Documented
public @interface PrimaryConstructor {
    
}

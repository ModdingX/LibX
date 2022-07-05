package org.moddingx.libx.impl.config.validators;

import org.moddingx.libx.LibX;
import org.moddingx.libx.config.validator.ConfigValidator;
import org.moddingx.libx.config.validator.ValidatorInfo;
import org.moddingx.libx.impl.config.ModMappers;
import org.moddingx.libx.util.ClassUtil;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConfiguredValidator<T, A extends Annotation> {
    
    private final ConfigValidator<T, A> validator;
    private final A annotation;

    public ConfiguredValidator(ConfigValidator<T, A> validator, A annotation) {
        this.validator = validator;
        this.annotation = annotation;
    }
    
    public T validate(T value, String action, List<String> path, @Nullable AtomicBoolean needsCorrection) {
        Optional<T> result = this.validator.validate(value, this.annotation);
        if (result.isPresent()) {
            LibX.logger.warn(action + ". Corrected value " + String.join(".", path) + " from " + value + " to " + result.get() + ".");
            if (needsCorrection != null) {
                needsCorrection.set(true);
            }
            return result.get();
        } else {
            return value;
        }
    }
    
    public Class<A> getAnnotationClass() {
        return this.validator.annotation();
    }
    
    public List<String> comment() {
        return this.validator.comment(this.annotation);
    }
    
    public ValidatorInfo<A> access() {
        return new ValidatorInfo<>() {
            
            @Nullable
            @Override
            public Class<A> type() {
                return ConfiguredValidator.this.validator.annotation();
            }

            @Nullable
            @Override
            public A value() {
                return ConfiguredValidator.this.annotation;
            }

            @Override
            public boolean isValid(Object value) {
                //noinspection unchecked
                return ConfiguredValidator.this.validator.type().isAssignableFrom(value.getClass()) && ConfiguredValidator.this.validator.validate((T) value, ConfiguredValidator.this.annotation).isEmpty();
            }
        };
    }

    @Nullable
    public static ConfiguredValidator<?, ?> create(String modid, Field field) {
        return create(modid, field, field.getType());
    }

    @Nullable
    public static ConfiguredValidator<?, ?> create(String modid, RecordComponent component) {
        return create(modid, component, component.getType());
    }
    
    @Nullable
    private static ConfiguredValidator<?, ?> create(String modid, AnnotatedElement element, Class<?> elementType) {
        ConfiguredValidator<?, ?> validator = null;
        for (Annotation annotation : element.getAnnotations()) {
            ConfigValidator<?, ?> v = ModMappers.get(modid).getValidatorByAnnotation(annotation.getClass());
            if (v != null) {
                if (validator != null) {
                    throw new IllegalStateException("A config key may only have one validator annotation but two are given: " + validator.getAnnotationClass().getName() + " and " + annotation.getClass().getName());
                } else if (!v.type().isAssignableFrom(ClassUtil.boxed(elementType))) {
                    throw new IllegalStateException("Invalid config validator annotation: @" + v.annotation().getSimpleName() + " requires elements of type " + v.type().getName() + " but was used on an element of type " + elementType.getName());
                } else {
                    //noinspection unchecked
                    validator = new ConfiguredValidator<>((ConfigValidator<Object, Annotation>) v, annotation);
                }
            }
        }
        return validator;
    }
}

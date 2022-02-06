package io.github.noeppi_noeppi.libx.impl.config;

import com.google.common.collect.ImmutableList;
import io.github.noeppi_noeppi.libx.config.Config;
import io.github.noeppi_noeppi.libx.config.ConfigValidator;
import io.github.noeppi_noeppi.libx.config.ValidatorInfo;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import io.github.noeppi_noeppi.libx.impl.config.validators.ConfiguredValidator;
import io.github.noeppi_noeppi.libx.util.ClassUtil;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConfigKey {
    
    public final Field field;
    public final ValueMapper<?, ?> mapper;
    public final List<String> path;
    public final List<String> comment;
    private final ConfiguredValidator<?, ?> validator;

    private ConfigKey(Field field, ValueMapper<?, ?> mapper, ImmutableList<String> path, ImmutableList<String> comment, ConfiguredValidator<?, ?> validator) {
        this.field = field;
        this.mapper = mapper;
        this.path = path;
        ImmutableList.Builder<String> commentBuilder = ImmutableList.builder();
        commentBuilder.addAll(comment);
        commentBuilder.addAll(mapper.comment());
        if (validator != null) {
            commentBuilder.addAll(validator.comment());
        }
        this.comment = commentBuilder.build();
        this.validator = validator;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        ConfigKey configKey = (ConfigKey) o;
        return this.field.equals(configKey.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.field);
    }
    
    public Object validate(Object value, String action, @Nullable AtomicBoolean needsCorrection) {
        if (!ClassUtil.boxed(this.field.getType()).isAssignableFrom(value.getClass())) {
            throw new IllegalStateException("LibX config internal error: Can't validate value of type " + value.getClass()  +" (expected " + this.field.getType() + ")");
        }
        if (this.validator != null) {
            //noinspection unchecked
            Object result = ((ConfiguredValidator<Object, ?>) this.validator).validate(value, action, this.path, needsCorrection);
            if (!ClassUtil.boxed(this.field.getType()).isAssignableFrom(result.getClass())) {
                throw new IllegalStateException("A config validator changed the type of a config key: " + result.getClass()  +" (expected " + this.field.getType() + ")");
            }
            return result;
        } else {
            return value;
        }
    }
    
    public ValidatorInfo<?> validatorAccess() {
        if (this.validator == null) {
            return ValidatorInfo.empty();
        } else {
            return this.validator.access();
        }
    }

    @Nullable
    public static ConfigKey create(String modid, Field field, Class<?> configBaseClass) {
        try {
            if (!Modifier.isStatic(field.getModifiers())) {
                return null;
            }
            Config config = field.getAnnotation(Config.class);
            if (config == null) {
                return null;
            }
            if (!config.modid().isEmpty() && !ModList.get().isLoaded(config.modid())) {
                return null;
            }
            field.setAccessible(true);
            List<String> path = new ArrayList<>();
            path.add(0, field.getName());
            Class<?> currentStep = field.getDeclaringClass();
            while (currentStep != configBaseClass) {
                if (currentStep == null || currentStep == Object.class) {
                    throw new IllegalStateException("LibX config internal error: Can't create config key for field that is not part of config base class.");
                }
                path.add(0, currentStep.getSimpleName());
                currentStep = currentStep.getDeclaringClass();
            }
            ConfiguredValidator<?, ?> validator = null;
            for (Annotation annotation : field.getAnnotations()) {
                ConfigValidator<?, ?> v = ModMappers.get(modid).getValidatorByAnnotation(annotation.getClass());
                if (v != null) {
                    if (validator != null) {
                        throw new IllegalStateException("A config key may only have one validator annotation but two are given: " + validator.getAnnotationClass().getName() + " and " + annotation.getClass().getName());
                    } else if (!v.type().isAssignableFrom(ClassUtil.boxed(field.getType()))) {
                        throw new IllegalStateException("Invalid config validator annotation: @" + v.annotation().getSimpleName() + " requires elements of type " + v.type().getName() + " but was used on an element of type " + field.getType().getName());
                    } else {
                        //noinspection unchecked
                        validator = new ConfiguredValidator<>((ConfigValidator<Object, Annotation>) v, annotation);
                    }
                }
            }
            return new ConfigKey(field, ModMappers.get(modid).getMapper(field), ImmutableList.copyOf(path), ImmutableList.copyOf(config.value()), validator);
        } catch (SecurityException e) {
            throw new IllegalStateException("Failed to create config key for field " + field, e);
        }
    }
    
    public static final Comparator<ConfigKey> BY_PATH = (o1, o2) -> {
        int minLength = Math.min(o1.path.size(), o2.path.size());
        for (int i = 0; i < minLength; i++) {
            int result = o1.path.get(i).compareTo(o2.path.get(i));
            if (result != 0) {
                return result;
            }
        }
        return Integer.compare(o1.path.size(), o2.path.size());
    };
}

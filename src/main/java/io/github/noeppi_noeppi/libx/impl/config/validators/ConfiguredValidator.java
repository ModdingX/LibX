package io.github.noeppi_noeppi.libx.impl.config.validators;

import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.config.ConfigValidator;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
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
}

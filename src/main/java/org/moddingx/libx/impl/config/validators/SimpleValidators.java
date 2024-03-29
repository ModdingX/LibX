package org.moddingx.libx.impl.config.validators;

import net.minecraft.util.Mth;
import org.moddingx.libx.config.validate.*;
import org.moddingx.libx.config.validator.ConfigValidator;

import java.util.List;
import java.util.Optional;

public class SimpleValidators {
    
    public static final ConfigValidator<Short, ShortRange> SHORT = new ConfigValidator<>() {
        @Override
        public Class<Short> type() {
            return Short.class;
        }

        @Override
        public Class<ShortRange> annotation() {
            return ShortRange.class;
        }

        @Override
        public Optional<Short> validate(Short value, ShortRange validator) {
            short clamped = (short) Mth.clamp(value, validator.min(), validator.max());
            return value == clamped ? Optional.empty() : Optional.of(clamped);
        }

        @Override
        public List<String> comment(ShortRange validator) {
            if (validator.min() == Short.MIN_VALUE && validator.max() == Short.MAX_VALUE) {
                return List.of();
            } else if (validator.max() == Short.MAX_VALUE) {
                return List.of("Minimum: " + validator.min());
            } else if (validator.min() == Short.MIN_VALUE) {
                return List.of("Maximum: " + validator.max());
            } else {
                return List.of("Range: " + validator.min() + " - " + validator.max());
            }
        }
    };

    public static final ConfigValidator<Integer, IntRange> INTEGER = new ConfigValidator<>() {
        @Override
        public Class<Integer> type() {
            return Integer.class;
        }

        @Override
        public Class<IntRange> annotation() {
            return IntRange.class;
        }

        @Override
        public Optional<Integer> validate(Integer value, IntRange validator) {
            int clamped = Mth.clamp(value, validator.min(), validator.max());
            return value == clamped ? Optional.empty() : Optional.of(clamped);
        }

        @Override
        public List<String> comment(IntRange validator) {
            if (validator.min() == Integer.MIN_VALUE && validator.max() == Integer.MAX_VALUE) {
                return List.of();
            } else if (validator.max() == Integer.MAX_VALUE) {
                return List.of("Minimum: " + validator.min());
            } else if (validator.min() == Integer.MIN_VALUE) {
                return List.of("Maximum: " + validator.max());
            } else {
                return List.of("Range: " + validator.min() + " - " + validator.max());
            }
        }
    };

    public static final ConfigValidator<Long, LongRange> LONG = new ConfigValidator<>() {
        @Override
        public Class<Long> type() {
            return Long.class;
        }

        @Override
        public Class<LongRange> annotation() {
            return LongRange.class;
        }

        @Override
        public Optional<Long> validate(Long value, LongRange validator) {
            long clamped = Math.max(Math.min(value, validator.max()), validator.min());
            return value == clamped ? Optional.empty() : Optional.of(clamped);
        }

        @Override
        public List<String> comment(LongRange validator) {
            if (validator.min() == Long.MIN_VALUE && validator.max() == Long.MAX_VALUE) {
                return List.of();
            } else if (validator.max() == Long.MAX_VALUE) {
                return List.of("Minimum: " + validator.min());
            } else if (validator.min() == Long.MIN_VALUE) {
                return List.of("Maximum: " + validator.max());
            } else {
                return List.of("Range: " + validator.min() + " - " + validator.max());
            }
        }
    };

    public static final ConfigValidator<Float, FloatRange> FLOAT = new ConfigValidator<>() {
        @Override
        public Class<Float> type() {
            return Float.class;
        }

        @Override
        public Class<FloatRange> annotation() {
            return FloatRange.class;
        }

        @Override
        public Optional<Float> validate(Float value, FloatRange validator) {
            float clamped = Mth.clamp(value, validator.min(), validator.max());
            return value == clamped ? Optional.empty() : Optional.of(clamped);
        }

        @Override
        public List<String> comment(FloatRange validator) {
            if (validator.min() == Float.NEGATIVE_INFINITY && validator.max() == Float.POSITIVE_INFINITY) {
                return List.of();
            } else if (validator.max() == Float.POSITIVE_INFINITY) {
                return List.of("Minimum: " + validator.min());
            } else if (validator.min() == Float.NEGATIVE_INFINITY) {
                return List.of("Maximum: " + validator.max());
            } else {
                return List.of("Range: " + validator.min() + " - " + validator.max());
            }
        }
    };

    public static final ConfigValidator<Double, DoubleRange> DOUBLE = new ConfigValidator<>() {
        @Override
        public Class<Double> type() {
            return Double.class;
        }

        @Override
        public Class<DoubleRange> annotation() {
            return DoubleRange.class;
        }

        @Override
        public Optional<Double> validate(Double value, DoubleRange validator) {
            double clamped = Mth.clamp(value, validator.min(), validator.max());
            return value == clamped ? Optional.empty() : Optional.of(clamped);
        }

        @Override
        public List<String> comment(DoubleRange validator) {
            if (validator.min() == Double.NEGATIVE_INFINITY && validator.max() == Double.POSITIVE_INFINITY) {
                return List.of();
            } else if (validator.max() == Double.POSITIVE_INFINITY) {
                return List.of("Minimum: " + validator.min());
            } else if (validator.min() == Double.NEGATIVE_INFINITY) {
                return List.of("Maximum: " + validator.max());
            } else {
                return List.of("Range: " + validator.min() + " - " + validator.max());
            }
        }
    };
}

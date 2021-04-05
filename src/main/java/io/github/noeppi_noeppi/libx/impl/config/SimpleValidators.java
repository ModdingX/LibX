package io.github.noeppi_noeppi.libx.impl.config;

import com.google.common.collect.ImmutableList;
import io.github.noeppi_noeppi.libx.config.ConfigValidator;
import io.github.noeppi_noeppi.libx.config.validator.*;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Optional;

public class SimpleValidators {
    
    public static final ConfigValidator<Short, ShortRange> SHORT = new ConfigValidator<Short, ShortRange>() {
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
            short clamped = (short) MathHelper.clamp(value, validator.min(), validator.max());
            return value == clamped ? Optional.empty() : Optional.of(clamped);
        }

        @Override
        public List<String> comment(ShortRange validator) {
            if (validator.min() == Short.MIN_VALUE && validator.max() == Short.MAX_VALUE) {
                return ImmutableList.of();
            } else if (validator.max() == Short.MAX_VALUE) {
                return ImmutableList.of("Minumum: " + validator.min());
            } else if (validator.min() == Short.MIN_VALUE) {
                return ImmutableList.of("Maximum: " + validator.max());
            } else {
                return ImmutableList.of("Range: " + validator.min() + " - " + validator.max());
            }
        }
    };

    public static final ConfigValidator<Integer, IntRange> INTEGER = new ConfigValidator<Integer, IntRange>() {
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
            int clamped = MathHelper.clamp(value, validator.min(), validator.max());
            return value == clamped ? Optional.empty() : Optional.of(clamped);
        }

        @Override
        public List<String> comment(IntRange validator) {
            if (validator.min() == Integer.MIN_VALUE && validator.max() == Integer.MAX_VALUE) {
                return ImmutableList.of();
            } else if (validator.max() == Integer.MAX_VALUE) {
                return ImmutableList.of("Minumum: " + validator.min());
            } else if (validator.min() == Integer.MIN_VALUE) {
                return ImmutableList.of("Maximum: " + validator.max());
            } else {
                return ImmutableList.of("Range: " + validator.min() + " - " + validator.max());
            }
        }
    };

    public static final ConfigValidator<Long, LongRange> LONG = new ConfigValidator<Long, LongRange>() {
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
            long clamped = MathHelper.clamp(value, validator.min(), validator.max());
            return value == clamped ? Optional.empty() : Optional.of(clamped);
        }

        @Override
        public List<String> comment(LongRange validator) {
            if (validator.min() == Long.MIN_VALUE && validator.max() == Long.MAX_VALUE) {
                return ImmutableList.of();
            } else if (validator.max() == Long.MAX_VALUE) {
                return ImmutableList.of("Minumum: " + validator.min());
            } else if (validator.min() == Long.MIN_VALUE) {
                return ImmutableList.of("Maximum: " + validator.max());
            } else {
                return ImmutableList.of("Range: " + validator.min() + " - " + validator.max());
            }
        }
    };

    public static final ConfigValidator<Float, FloatRange> FLOAT = new ConfigValidator<Float, FloatRange>() {
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
            float clamped = MathHelper.clamp(value, validator.min(), validator.max());
            return value == clamped ? Optional.empty() : Optional.of(clamped);
        }

        @Override
        public List<String> comment(FloatRange validator) {
            if (validator.min() == Float.NEGATIVE_INFINITY && validator.max() == Float.POSITIVE_INFINITY) {
                return ImmutableList.of();
            } else if (validator.max() == Float.POSITIVE_INFINITY) {
                return ImmutableList.of("Minumum: " + validator.min());
            } else if (validator.min() == Float.NEGATIVE_INFINITY) {
                return ImmutableList.of("Maximum: " + validator.max());
            } else {
                return ImmutableList.of("Range: " + validator.min() + " - " + validator.max());
            }
        }
    };

    public static final ConfigValidator<Double, DoubleRange> DOUBLE = new ConfigValidator<Double, DoubleRange>() {
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
            double clamped = MathHelper.clamp(value, validator.min(), validator.max());
            return value == clamped ? Optional.empty() : Optional.of(clamped);
        }

        @Override
        public List<String> comment(DoubleRange validator) {
            if (validator.min() == Double.NEGATIVE_INFINITY && validator.max() == Double.POSITIVE_INFINITY) {
                return ImmutableList.of();
            } else if (validator.max() == Double.POSITIVE_INFINITY) {
                return ImmutableList.of("Minumum: " + validator.min());
            } else if (validator.min() == Double.NEGATIVE_INFINITY) {
                return ImmutableList.of("Maximum: " + validator.max());
            } else {
                return ImmutableList.of("Range: " + validator.min() + " - " + validator.max());
            }
        }
    };
}

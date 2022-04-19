package io.github.noeppi_noeppi.libx.registration.util;

import com.google.common.collect.ImmutableMap;
import io.github.noeppi_noeppi.libx.registration.MultiRegisterable;
import io.github.noeppi_noeppi.libx.registration.RegistrationContext;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

public class EnumObjects<E extends Enum<E>, T> implements MultiRegisterable<T> {

    private final T defaultValue;
    private final E[] keys;
    private final Map<E, T> map;
    
    public EnumObjects(Class<E> cls, Function<E, T> factory) {
        if (!cls.isEnum()) {
            throw new IllegalStateException("Non-enum class in EnumObjects: " + cls.getName());
        }
        T defaultValue = null;
        ImmutableMap.Builder<E, T> builder = ImmutableMap.builder();
        this.keys = cls.getEnumConstants();
        if (this.keys.length == 0) {
            throw new IllegalStateException("EnumObjects cannot be used with empty enums.");
        }
        for (E e : this.keys) {
            T t = factory.apply(e);
            if (defaultValue == null) defaultValue = t;
            builder.put(e, t);
        }
        this.defaultValue = Objects.requireNonNull(defaultValue, "EnumObjects cannot be used with empty enums.");
        this.map = builder.build();
    }
    
    public T get(E key) {
        return this.map.getOrDefault(key, this.defaultValue);
    }
    
    public T random(Random random) {
        return this.map.get(this.keys[random.nextInt(this.keys.length)]);
    }

    @Override
    public void buildAdditionalRegisters(RegistrationContext ctx, EntryCollector<T> builder) {
        for (Map.Entry<E, T> entry : this.map.entrySet()) {
            builder.registerNamed(entry.getKey().name().toLowerCase(Locale.ROOT), entry.getValue());
        }
    }
}

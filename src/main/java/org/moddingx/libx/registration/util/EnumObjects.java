package org.moddingx.libx.registration.util;

import com.google.common.collect.ImmutableMap;
import org.moddingx.libx.registration.MultiRegisterable;
import org.moddingx.libx.registration.RegistrationContext;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

/**
 * A {@link MultiRegisterable} that registers multiple objects, one for each value of an enum. This is
 * done via {@link MultiRegisterable#registerAdditional(RegistrationContext, EntryCollector)} so
 * the enum names will be applied automatically.
 *
 * @param <E> The type of the enum to use.
 * @param <T> The type of the thing to register.
 */
public class EnumObjects<E extends Enum<E>, T> implements MultiRegisterable<T> {

    private final T defaultValue;
    private final E[] keys;
    private final Map<E, T> map;

    /**
     * Creates a new instance of EnumObjects.
     *
     * @param cls The class of the enum that is used. The enum must have at least one value.
     * @param factory A factory function that creates the objects to be registered.
     */
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

    /**
     * Gets a value for an enum value.
     */
    public T get(E key) {
        return this.map.getOrDefault(key, this.defaultValue);
    }

    /**
     * Gets a random object from the objects in this EnumObjects.
     */
    public T random(Random random) {
        return this.map.get(this.keys[random.nextInt(this.keys.length)]);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void registerAdditional(RegistrationContext ctx, EntryCollector<T> builder) {
        for (Map.Entry<E, T> entry : this.map.entrySet()) {
            builder.registerNamed(entry.getKey().name().toLowerCase(Locale.ROOT), entry.getValue());
        }
    }
}

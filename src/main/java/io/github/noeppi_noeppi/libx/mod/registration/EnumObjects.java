package io.github.noeppi_noeppi.libx.mod.registration;

import com.google.common.collect.ImmutableMap;
import io.github.noeppi_noeppi.libx.annotation.meta.RemoveIn;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

/**
 * A {@link Registerable} that registers multiple objects, one for each value of an enum. This is
 * done via {@link Registerable#getNamedAdditionalRegisters(ResourceLocation)} so the enum names will be
 * applied automatically.
 *
 * @param <E> The type of the enum to use.
 * @param <T> The type of the thing to register.
 *
 * @deprecated See https://gist.github.com/noeppi-noeppi/9de9b6af950ee02f2dee611742fe2d6d
 */
@Deprecated(forRemoval = true)
@RemoveIn(minecraft = "1.19")
public class EnumObjects<E extends Enum<E>, T> implements Registerable {
    
    private final T defaultValue;
    private final E[] keys;
    private final Map<E, T> map;
    
    /**
     * Creates a new instance of EnumObjects.
     *
     * @param enumClass The class of the enum that is used.
     * @param factory A factory function that creates the objects to be registered.
     */
    public EnumObjects(Class<E> enumClass, Function<E, T> factory) {
        if (!enumClass.isEnum()) {
            throw new IllegalStateException("EnumObjects can only be used with enum classes.");
        }
        T defaultValue = null;
        ImmutableMap.Builder<E, T> builder = ImmutableMap.builder();
        this.keys = enumClass.getEnumConstants();
        if (this.keys.length == 0) {
            throw new IllegalStateException("EnumObjects can not be used with empty enums.");
        }
        for (E e : this.keys) {
            T t = factory.apply(e);
            if (defaultValue == null) defaultValue = t;
            builder.put(e, t);
        }
        this.defaultValue = Objects.requireNonNull(defaultValue, "No value in EnumObjects. This can not be used with empty enums.");
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
    public Map<String, Object> getNamedAdditionalRegisters(ResourceLocation id) {
        return this.map.entrySet().stream()
                .map(e -> Pair.of(e.getKey().name().toLowerCase(Locale.ROOT), e.getValue()))
                .collect(ImmutableMap.toImmutableMap(Pair::getKey, Pair::getValue));
    }

    /**
     * Creates a new EnumObjects for DyeColors.
     */
    public static <T> EnumObjects<DyeColor, T> colored(Function<DyeColor, T> factory) {
        return new EnumObjects<>(DyeColor.class, factory);
    }
}

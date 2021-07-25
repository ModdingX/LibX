package io.github.noeppi_noeppi.libx.impl.config;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.config.*;
import io.github.noeppi_noeppi.libx.impl.config.mappers.SimpleValueMappers;
import io.github.noeppi_noeppi.libx.impl.config.mappers.advanced.*;
import io.github.noeppi_noeppi.libx.impl.config.mappers.generic.ListValueMapper;
import io.github.noeppi_noeppi.libx.impl.config.mappers.generic.MapValueMapper;
import io.github.noeppi_noeppi.libx.impl.config.mappers.generic.OptionValueMapper;
import io.github.noeppi_noeppi.libx.impl.config.mappers.special.EnumValueMappers;
import io.github.noeppi_noeppi.libx.impl.config.mappers.special.PairValueMapper;
import io.github.noeppi_noeppi.libx.impl.config.mappers.special.RecordValueMapper;
import io.github.noeppi_noeppi.libx.impl.config.mappers.special.TripleValueMapper;
import io.github.noeppi_noeppi.libx.impl.config.validators.SimpleValidators;
import io.github.noeppi_noeppi.libx.impl.config.wrapper.JsonTypesafeMapper;
import io.github.noeppi_noeppi.libx.impl.config.wrapper.WrappedGenericMapper;
import io.github.noeppi_noeppi.libx.util.ClassUtil;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.OnlyIns;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;

// special allowed types
// enums
// pair
// triple
// record
public class ModMappers {

    private static final Map<String, ModMappers> modMappers = new HashMap<>();

    public static ModMappers get(String modid) {
        synchronized (modMappers) {
            if (!modMappers.containsKey(modid)) {
                modMappers.put(modid, new ModMappers(modid));
            }
            return modMappers.get(modid);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private static final Map<Class<?>, CommonValueMapper<?, ?>> globalMappers = Set.of(
            SimpleValueMappers.BOOLEAN,
            SimpleValueMappers.BYTE,
            SimpleValueMappers.SHORT,
            SimpleValueMappers.INTEGER,
            SimpleValueMappers.LONG,
            SimpleValueMappers.FLOAT,
            SimpleValueMappers.DOUBLE,
            SimpleValueMappers.STRING,
            OptionValueMapper.INSTANCE,
            ListValueMapper.INSTANCE,
            MapValueMapper.INSTANCE,
            ResourceValueMapper.INSTANCE,
            IngredientValueMapper.INSTANCE,
            ComponentValueMapper.INSTANCE,
            ResourceListValueMapper.INSTANCE,
            IngredientStackValueMapper.INSTANCE,
            UidValueMapper.INSTANCE
    ).stream().collect(ImmutableMap.toImmutableMap(CommonValueMapper::type, Function.identity()));

    @SuppressWarnings("UnstableApiUsage")
    private static final Map<Class<? extends Annotation>, ConfigValidator<?, ?>> globalValidators = Set.of(
            SimpleValidators.SHORT,
            SimpleValidators.INTEGER,
            SimpleValidators.LONG,
            SimpleValidators.FLOAT,
            SimpleValidators.DOUBLE
    ).stream().collect(ImmutableMap.toImmutableMap(ConfigValidator::annotation, Function.identity()));


    private final String modid;
    private final Map<Class<?>, CommonValueMapper<?, ?>> mappers = Collections.synchronizedMap(new HashMap<>());
    private final Map<Class<? extends Annotation>, ConfigValidator<?, ?>> validators = Collections.synchronizedMap(new HashMap<>());

    private ModMappers(String modid) {
        this.modid = modid;
    }

    public void registerValueMapper(ValueMapper<?, ?> mapper) {
        this.doRegisterValueMapper(mapper);
    }

    public void registerValueMapper(GenericValueMapper<?, ?, ?> mapper) {
        this.doRegisterValueMapper(mapper);
    }

    private void doRegisterValueMapper(CommonValueMapper<?, ?> mapper) {
        if (this.mappers.containsKey(mapper.type())) {
            throw new IllegalStateException("Config mapper for type '" + mapper.type() + "' is already registered.");
        } else {
            if (globalMappers.containsKey(mapper.type())) {
                LibX.logger.warn(this.modid + " registers a custom value mapper for type " + mapper.type() + " even if there's a builtin one. This is discouraged.");
            }
            this.mappers.put(mapper.type(), mapper);
        }
    }

    public void registerConfigValidator(ConfigValidator<?, ?> validator) {
        if (this.validators.containsKey(validator.annotation())) {
            throw new IllegalStateException("Config validator for annotation '" + validator.annotation() + "' is already registered.");
        } else if (globalValidators.containsKey(validator.annotation())) {
            throw new IllegalStateException("Config validator for annotation '" + validator.annotation() + "' is global, can't be changed. Add your own annotation.");
        } else {
            this.validators.put(validator.annotation(), validator);
        }
    }

    public ValueMapper<?, ?> getMapper(Field field) {
        return this.getMapper(field.getGenericType());
    }

    private ValueMapper<?, ?> getMapper(Type type) {
        Class<?> cls = ClassUtil.boxed(getTypeClass(type));
        if (cls.isEnum()) {
            //noinspection unchecked
            return EnumValueMappers.getMapper((Class<? extends Enum<?>>) cls);
        } else if (cls == Pair.class) {
            return new PairValueMapper<>(this.getWrappedMapperUnsafe(type, 0), this.getWrappedMapperUnsafe(type, 1));
        } else if (cls == Triple.class) {
            return new TripleValueMapper<>(this.getWrappedMapperUnsafe(type, 0), this.getWrappedMapperUnsafe(type, 1), this.getWrappedMapperUnsafe(type, 2));
        } else if (cls.isRecord()) {
            //noinspection unchecked
            return new RecordValueMapper<>((Class<? extends Record>) cls, this::getMapper);
        }
        CommonValueMapper<?, ?> mapper = null;
        if (this.mappers.containsKey(cls)) {
            mapper = this.mappers.get(cls);
        } else if (globalMappers.containsKey(cls)) {
            mapper = globalMappers.get(cls);
        }
        if (mapper instanceof ValueMapper<?, ?> valueMapper) {
            return valueMapper;
        } else if (mapper instanceof GenericValueMapper<?, ?, ?> genericMapper) {
            if (!(type instanceof ParameterizedType)) {
                throw new IllegalStateException("Generic value mapper used on type without generics.");
            }
            Type[] args = ((ParameterizedType) type).getActualTypeArguments();
            if (args.length <= genericMapper.getGenericElementPosition()) {
                throw new IllegalStateException("Generic value mapper used on type with too less generics: Expected at least " + (genericMapper.getGenericElementPosition() + 1) + ", got " + args.length);
            }
            ValueMapper<Object, JsonElement> childMapper = this.getWrappedMapperUnsafe(type, genericMapper.getGenericElementPosition());
            //noinspection unchecked
            return new WrappedGenericMapper<>((GenericValueMapper<Object, JsonElement, Object>) genericMapper, childMapper);
        } else {
            throw new IllegalStateException("No config mapper found for type " + type + " (" + cls + ")");
        }
    }

    private ValueMapper<Object, JsonElement> getWrappedMapperUnsafe(Type type, int genericPosition) {
        if (!(type instanceof ParameterizedType)) {
            throw new IllegalStateException("Generic value mapper used on type without generics.");
        }
        Type[] args = ((ParameterizedType) type).getActualTypeArguments();
        if (args.length <= genericPosition) {
            throw new IllegalStateException("Generic value mapper used on type with too less generics: Expected at least " + (genericPosition + 1) + ", got " + args.length);
        }
        ValueMapper<?, ?> mapper = this.getMapper(args[genericPosition]);
        //noinspection unchecked
        return new JsonTypesafeMapper<>((ValueMapper<Object, ?>) mapper);
    }
    
    private static Class<?> getTypeClass(Type type) {
        if (type instanceof Class<?> cls) {
            return cls;
        } else if (type instanceof ParameterizedType ptype) {
            return getTypeClass(ptype.getRawType());
        } else if (type instanceof TypeVariable) {
            throw new IllegalStateException("Type variables are not allowed in config field types.");
        } else if (type instanceof WildcardType) {
            throw new IllegalStateException("Wildcard types are not allowed in config field types.");
        } else {
            throw new IllegalStateException("Unknown declared type of config field: " + type);
        }
    }

    @Nullable
    public <A extends Annotation> ConfigValidator<?, A> getValidatorByAnnotation(Class<A> validatorClass) {
        // Annotations will be proxies at runtime so we can't check classes for equality.
        if (Config.class.isAssignableFrom(validatorClass) || Group.class.isAssignableFrom(validatorClass)
                || OnlyIn.class.isAssignableFrom(validatorClass) || OnlyIns.class.isAssignableFrom(validatorClass)) {
            // Just in case someone register those...
            return null;
        } else {
            Optional<? extends ConfigValidator<?, ?>> validator = globalValidators.entrySet().stream()
                    .filter(e -> e.getKey().isAssignableFrom(validatorClass))
                    .map(Map.Entry::getValue)
                    .findFirst();
            if (validator.isPresent()) {
                //noinspection unchecked
                return (ConfigValidator<?, A>) validator.get();
            } else {
                validator = this.validators.entrySet().stream()
                        .filter(e -> e.getKey().isAssignableFrom(validatorClass))
                        .map(Map.Entry::getValue)
                        .findFirst();
                //noinspection unchecked
                return (ConfigValidator<?, A>) validator.orElse(null);
            }
        }
    }
}

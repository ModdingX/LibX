package org.moddingx.libx.impl.config.mappers.special;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.moddingx.libx.LibX;
import org.moddingx.libx.config.correct.ConfigCorrection;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;
import org.moddingx.libx.impl.config.gui.editor.RecordEditor;
import org.moddingx.libx.impl.config.validators.ConfiguredValidator;
import org.moddingx.libx.impl.config.wrapper.TypesafeMapper;
import org.moddingx.libx.util.ClassUtil;

import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;

public class RecordValueMapper<T extends Record> implements ValueMapper<T, JsonObject> {
    
    private final Class<T> clazz;
    private final List<EntryData> entries;
    private final Constructor<T> ctor;

    public RecordValueMapper(String modid, Class<T> clazz, Function<Type, ValueMapper<?, ?>> mapperFunc) {
        this.clazz = clazz;
        if (!clazz.isRecord()) {
            throw new IllegalArgumentException("Can't create record config value mapper for non-record class.");
        }
        RecordComponent[] parts = this.clazz.getRecordComponents();
        Class<?>[] types = new Class<?>[parts.length];
        ImmutableList.Builder<EntryData> entries = ImmutableList.builder();
        for (int i = 0; i < parts.length; i++) {
            types[i] = parts[i].getType();
            TypesafeMapper mapper = new TypesafeMapper(mapperFunc.apply(parts[i].getGenericType()));
            ConfiguredValidator<?, ?> validator = ConfiguredValidator.create(modid, parts[i]);
            entries.add(new EntryData(mapper, validator));
        }
        this.entries = entries.build();
        if (this.entries.isEmpty()) {
            throw new IllegalArgumentException("Can't create record config value mapper for empty record.");
        }
        try {
            // Must be explicitly set accessible
            // While the constructor itself is always public, the record class may be private
            this.ctor = this.clazz.getDeclaredConstructor(types);
            this.ctor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Can't create record config value mapper for class: " + clazz, e);
        }
    }

    @Override
    public Class<T> type() {
        return this.clazz;
    }

    @Override
    public Class<JsonObject> element() {
        return JsonObject.class;
    }

    @Override
    public T fromJson(JsonObject json) {
        RecordComponent[] parts = this.clazz.getRecordComponents();
        Object[] values = new Object[parts.length];
        for (int i = 0; i < parts.length; i++) {
            values[i] = this.entries.get(i).mapper().fromJson(json.get(parts[i].getName()));
        }
        try {
            return this.ctor.newInstance(values);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to create record for config.", e);
        }
    }

    @Override
    public JsonObject toJson(T value) {
        JsonObject json = new JsonObject();
        RecordComponent[] parts = this.clazz.getRecordComponents();
        for (int i = 0; i < parts.length; i++) {
            try {
                json.add(parts[i].getName(), this.entries.get(i).mapper().toJson(accessComponent(parts[i], value)));
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to get record value for config.", e);
            }
        }
        return json;
    }
    
    public T validate(T value, String action, List<String> path, @Nullable AtomicBoolean needsCorrection) {
        RecordComponent[] parts = this.clazz.getRecordComponents();
        Object[] values = new Object[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                List<String> thePath = Stream.concat(path.stream(), Stream.of(parts[i].getName())).toList();
                Object oldValue = accessComponent(parts[i], value);
                
                @SuppressWarnings("unchecked")
                ConfiguredValidator<Object, ?> validatorUnsafe = ((ConfiguredValidator<Object, ?>) this.entries.get(i).validator());
                if (validatorUnsafe != null) {
                    Object newValue = validatorUnsafe.validate(oldValue, action, thePath, needsCorrection);
                    if (!ClassUtil.boxed(parts[i].getType()).isAssignableFrom(newValue.getClass())) {
                        throw new IllegalStateException("A config validator changed the type of a record key: " + newValue.getClass()  +" (expected " + parts[i].getType() + ")");
                    }
                    values[i] = newValue;
                } else {
                    values[i] = oldValue;
                }
            } catch (ReflectiveOperationException e) {
                LibX.logger.error("Failed to correct record value for config.", e);
                if (needsCorrection != null) {
                    needsCorrection.set(true);
                }
                return value;
            }
        }
        try {
            return this.ctor.newInstance(values);
        } catch (ReflectiveOperationException e) {
            LibX.logger.error("Failed to create record for corrected config.", e);
            if (needsCorrection != null) {
                needsCorrection.set(true);
            }
            return value;
        }
    }

    @Override
    public T fromNetwork(FriendlyByteBuf buffer) {
        RecordComponent[] parts = this.clazz.getRecordComponents();
        Object[] values = new Object[parts.length];
        for (int i = 0; i < parts.length; i++) {
            values[i] = this.entries.get(i).mapper().fromNetwork(buffer);
        }
        try {
            return this.ctor.newInstance(values);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to create record for config.", e);
        }
    }

    @Override
    public void toNetwork(T value, FriendlyByteBuf buffer) {
        RecordComponent[] parts = this.clazz.getRecordComponents();
        for (int i = 0; i < parts.length; i++) {
            try {
                this.entries.get(i).mapper().toNetwork(accessComponent(parts[i], value), buffer);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to get record value for config.", e);
            }
        }
    }

    @Override
    public Optional<T> correct(JsonElement json, ConfigCorrection<T> correction) {
        RecordComponent[] parts = this.clazz.getRecordComponents();
        if (json.isJsonObject()) {
            // We have a json object. Just correct every key from it.
            Object[] args = new Object[parts.length];
            for (int i = 0; i < parts.length; i++) {
                int idx = i;
                String name = parts[i].getName();
                Optional<Object> obj = correction.tryCorrect(json.getAsJsonObject().has(name) ? json.getAsJsonObject().get(name) : null, this.entries.get(idx).mapper(), record -> {
                    try {
                        return Optional.of(accessComponent(parts[idx], record));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        return Optional.empty();
                    }
                });
                if (obj.isPresent()) {
                    args[i] = obj.get();
                } else {
                    return Optional.empty();
                }
            }
            try {
                return Optional.of(this.ctor.newInstance(args));
            } catch (ReflectiveOperationException e) {
                return Optional.empty();
            }
        } else if (parts.length == 1) {
            // Probably just forgot the json object around.
            Optional<Object> singleArgValue = correction.tryCorrect(json, this.entries.get(0).mapper(), record -> {
                try {
                    return Optional.of(accessComponent(parts[0], record));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    return Optional.empty();
                }
            });
            return singleArgValue.flatMap(value -> {
                try {
                    return Optional.of(this.ctor.newInstance(value));
                } catch (ReflectiveOperationException e) {
                    return Optional.empty();
                }
            });
        } else {
            return Optional.empty();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ConfigEditor<T> createEditor(ValidatorInfo<?> validator) {
        return new RecordEditor<>(this.clazz, this.entries, this.ctor);
    }
    
    public static Object accessComponent(RecordComponent component, Object instance) throws InvocationTargetException, IllegalAccessException {
        Method method = component.getAccessor();
        method.setAccessible(true); // Needed if the record class itself is private
        return method.invoke(instance);
    }
    
    public record EntryData(TypesafeMapper mapper, @Nullable ConfiguredValidator<?, ?> validator) {}
}

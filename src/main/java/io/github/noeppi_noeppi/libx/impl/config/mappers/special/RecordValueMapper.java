package io.github.noeppi_noeppi.libx.impl.config.mappers.special;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import io.github.noeppi_noeppi.libx.config.correct.ConfigCorrection;
import io.github.noeppi_noeppi.libx.impl.config.wrapper.TypesafeMapper;
import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class RecordValueMapper<T extends Record> implements ValueMapper<T, JsonObject> {
    
    private final Class<T> clazz;
    private final List<TypesafeMapper> mappers;
    private final Constructor<T> ctor;

    public RecordValueMapper(Class<T> clazz, Function<Type, ValueMapper<?, ?>> mapperFunc) {
        this.clazz = clazz;
        if (!clazz.isRecord()) {
            throw new IllegalArgumentException("Can't create record config value mapper for non-record class.");
        }
        RecordComponent[] parts = this.clazz.getRecordComponents();
        Class<?>[] types = new Class<?>[parts.length];
        ImmutableList.Builder<TypesafeMapper> mappers = ImmutableList.builder();
        for (int i = 0; i < parts.length; i++) {
            types[i] = parts[i].getType();
            mappers.add(new TypesafeMapper(mapperFunc.apply(parts[i].getGenericType())));
        }
        this.mappers = mappers.build();
        if (this.mappers.isEmpty()) {
            throw new IllegalArgumentException("Can't create record config value mapper for empty record.");
        }
        try {
            this.ctor = this.clazz.getConstructor(types);
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
            values[i] = this.mappers.get(i).fromJson(json.get(parts[i].getName()));
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
                json.add(parts[i].getName(), this.mappers.get(i).toJson(parts[i].getAccessor().invoke(value)));
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to get record value for config.", e);
            }
        }
        return json;
    }

    @Override
    public T fromNetwork(FriendlyByteBuf buffer) {
        RecordComponent[] parts = this.clazz.getRecordComponents();
        Object[] values = new Object[parts.length];
        for (int i = 0; i < parts.length; i++) {
            values[i] = this.mappers.get(i).fromNetwork(buffer);
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
                this.mappers.get(i).toNetwork(parts[i].getAccessor().invoke(value), buffer);
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
                Optional<Object> obj = correction.tryCorrect(json.getAsJsonObject().has(name) ? json.getAsJsonObject().get(name) : null, this.mappers.get(idx), record -> {
                    try {
                        return Optional.of(parts[idx].getAccessor().invoke(record));
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
            Optional<Object> singleArgValue = correction.tryCorrect(json, this.mappers.get(0), record -> {
                try {
                    return Optional.of(parts[0].getAccessor().invoke(record));
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
}

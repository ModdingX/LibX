package io.github.noeppi_noeppi.libx.impl.config.mappers.special;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import io.github.noeppi_noeppi.libx.impl.config.wrapper.TypesafeMapper;
import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;

public class RecordValueMapper<T extends Record> implements ValueMapper<T, JsonObject> {
    
    private final Class<T> clazz;
    private final List<TypesafeMapper> mappers;

    public RecordValueMapper(Class<T> clazz, Function<Type, ValueMapper<?, ?>> mapperFunc) {
        this.clazz = clazz;
        if (!clazz.isRecord()) {
            throw new IllegalArgumentException("Can't create record config value mapper for non-record class.");
        }
        ImmutableList.Builder<TypesafeMapper> mappers = ImmutableList.builder();
        for (RecordComponent part : clazz.getRecordComponents()) {
            mappers.add(new TypesafeMapper(mapperFunc.apply(part.getGenericType())));
        }
        this.mappers = mappers.build();
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
    public T fromJSON(JsonObject json) {
        RecordComponent[] parts = this.clazz.getRecordComponents();
        Class<?>[] types = new Class<?>[parts.length];
        Object[] values = new Object[parts.length];
        for (int i = 0; i < parts.length; i++) {
            types[i] = parts[i].getType();
            values[i] = this.mappers.get(i).fromJSON(json.get(parts[i].getName()));
        }
        try {
            Constructor<T> ctor = this.clazz.getConstructor(types);
            return ctor.newInstance(values);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to create record for config.", e);
        }
    }

    @Override
    public JsonObject toJSON(T value) {
        JsonObject json = new JsonObject();
        RecordComponent[] parts = this.clazz.getRecordComponents();
        for (int i = 0; i < parts.length; i++) {
            try {
                json.add(parts[i].getName(), this.mappers.get(i).toJSON(parts[i].getAccessor().invoke(value)));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Failed to get record value for config.", e);
            }
        }
        return json;
    }

    @Override
    public T read(FriendlyByteBuf buffer) {
        RecordComponent[] parts = this.clazz.getRecordComponents();
        Class<?>[] types = new Class<?>[parts.length];
        Object[] values = new Object[parts.length];
        for (int i = 0; i < parts.length; i++) {
            types[i] = parts[i].getType();
            values[i] = this.mappers.get(i).read(buffer);
        }
        try {
            Constructor<T> ctor = this.clazz.getConstructor(types);
            return ctor.newInstance(values);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to create record for config.", e);
        }
    }

    @Override
    public void write(T value, FriendlyByteBuf buffer) {
        RecordComponent[] parts = this.clazz.getRecordComponents();
        for (int i = 0; i < parts.length; i++) {
            try {
                this.mappers.get(i).write(parts[i].getAccessor().invoke(value), buffer);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Failed to get record value for config.", e);
            }
        }
    }
}

package io.github.noeppi_noeppi.libx.impl.config.mappers.generic;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.github.noeppi_noeppi.libx.config.GenericValueMapper;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public class ListValueMapper<T> implements GenericValueMapper<List<T>, JsonArray, T> {

    public static final ListValueMapper<?> INSTANCE = new ListValueMapper<>();
    
    private ListValueMapper() {
        
    }
    
    @Override
    public Class<List<T>> type() {
        //noinspection unchecked
        return (Class<List<T>>) (Class<?>) List.class;
    }

    @Override
    public Class<JsonArray> element() {
        return JsonArray.class;
    }

    @Override
    public int getGenericElementPosition() {
        return 0;
    }

    @Override
    public List<T> fromJSON(JsonArray json, ValueMapper<T, JsonElement> mapper) {
        ImmutableList.Builder<T> builder = ImmutableList.builder();
        for (int i = 0; i < json.size(); i++) {
            JsonElement element = json.get(i);
            builder.add(mapper.fromJSON(element));
        }
        return builder.build();
    }

    @Override
    public JsonArray toJSON(List<T> value, ValueMapper<T, JsonElement> mapper) {
        JsonArray array = new JsonArray();
        for (T element : value) {
            array.add(mapper.toJSON(element));
        }
        return array;
    }

    @Override
    public List<T> read(FriendlyByteBuf buffer, ValueMapper<T, JsonElement> mapper) {
        int size = buffer.readVarInt();
        ImmutableList.Builder<T> builder = ImmutableList.builder();
        for (int i = 0; i < size; i++) {
            builder.add(mapper.read(buffer));
        }
        return builder.build();
    }

    @Override
    public void write(List<T> value, FriendlyByteBuf buffer, ValueMapper<T, JsonElement> mapper) {
        buffer.writeVarInt(value.size());
        for (T element : value) {
            mapper.write(element, buffer);
        }
    }
}

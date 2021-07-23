package io.github.noeppi_noeppi.libx.impl.config.mappers.generic;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.noeppi_noeppi.libx.config.GenericValueMapper;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Map;

public class MapValueMapper<T> implements GenericValueMapper<Map<String, T>, JsonObject, T> {

    public static final MapValueMapper<?> INSTANCE = new MapValueMapper<>();

    private MapValueMapper() {

    }

    @Override
    public Class<Map<String, T>> type() {
        //noinspection unchecked
        return (Class<Map<String, T>>) (Class<?>) Map.class;
    }

    @Override
    public Class<JsonObject> element() {
        return JsonObject.class;
    }

    @Override
    public int getGenericElementPosition() {
        return 1;
    }

    @Override
    public Map<String, T> fromJSON(JsonObject json, ValueMapper<T, JsonElement> mapper) {
        ImmutableMap.Builder<String, T> builder = ImmutableMap.builder();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            builder.put(entry.getKey(), mapper.fromJSON(entry.getValue()));
        }
        return builder.build();
    }

    @Override
    public JsonObject toJSON(Map<String, T> value, ValueMapper<T, JsonElement> mapper) {
        JsonObject object = new JsonObject();
        for (Map.Entry<String, T> entry : value.entrySet()) {
            object.add(entry.getKey(), mapper.toJSON(entry.getValue()));
        }
        return object;
    }

    @Override
    public Map<String, T> read(FriendlyByteBuf buffer, ValueMapper<T, JsonElement> mapper) {
        int size = buffer.readVarInt();
        ImmutableMap.Builder<String, T> builder = ImmutableMap.builder();
        for (int i = 0; i < size; i++) {
            builder.put(buffer.readUtf(0x7fff), mapper.read(buffer));
        }
        return builder.build();
    }

    @Override
    public void write(Map<String, T> value, FriendlyByteBuf buffer, ValueMapper<T, JsonElement> mapper) {
        buffer.writeVarInt(value.size());
        for (Map.Entry<String, T> entry : value.entrySet()) {
            buffer.writeUtf(entry.getKey(), 0x7fff);
            mapper.write(entry.getValue(), buffer);
        }
    }
}

package org.moddingx.libx.impl.config.mappers.generic;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.moddingx.libx.config.correct.ConfigCorrection;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.mapper.GenericValueMapper;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;
import org.moddingx.libx.impl.config.gui.screen.content.MapContent;

import java.util.Map;
import java.util.Optional;

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
    public Map<String, T> fromJson(JsonObject json, ValueMapper<T, JsonElement> mapper) {
        ImmutableMap.Builder<String, T> builder = ImmutableMap.builder();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            builder.put(entry.getKey(), mapper.fromJson(entry.getValue()));
        }
        return builder.build();
    }

    @Override
    public JsonObject toJson(Map<String, T> value, ValueMapper<T, JsonElement> mapper) {
        JsonObject object = new JsonObject();
        for (Map.Entry<String, T> entry : value.entrySet()) {
            object.add(entry.getKey(), mapper.toJson(entry.getValue()));
        }
        return object;
    }

    @Override
    public Map<String, T> fromNetwork(FriendlyByteBuf buffer, ValueMapper<T, JsonElement> mapper) {
        int size = buffer.readVarInt();
        ImmutableMap.Builder<String, T> builder = ImmutableMap.builder();
        for (int i = 0; i < size; i++) {
            builder.put(buffer.readUtf(0x7fff), mapper.fromNetwork(buffer));
        }
        return builder.build();
    }

    @Override
    public void toNetwork(Map<String, T> value, FriendlyByteBuf buffer, ValueMapper<T, JsonElement> mapper) {
        buffer.writeVarInt(value.size());
        for (Map.Entry<String, T> entry : value.entrySet()) {
            buffer.writeUtf(entry.getKey(), 0x7fff);
            mapper.toNetwork(entry.getValue(), buffer);
        }
    }

    @Override
    public Optional<Map<String, T>> correct(JsonElement json, ValueMapper<T, JsonElement> mapper, ConfigCorrection<Map<String, T>> correction) {
        if (json.isJsonObject()) {
            ImmutableMap.Builder<String, T> map = ImmutableMap.builder();
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                correction.tryCorrect(entry.getValue(), mapper, value -> Optional.ofNullable(value.getOrDefault(entry.getKey(), null))).ifPresent(value -> map.put(entry.getKey(), value));
            }
            Map<String, T> result = map.build();
            if (result.isEmpty() && json.getAsJsonObject().size() > 0) {
                // Nothing matched. Might be better to return the default value here
                return Optional.empty();
            } else {
                return Optional.of(result);
            }
        } else if (json.isJsonArray() && json.getAsJsonArray().size() == 2 && (json.getAsJsonArray().get(0).isJsonPrimitive() || json.getAsJsonArray().get(0).isJsonNull())) {
            // Treat this as a single key/value pair
            String key = json.getAsJsonArray().get(0).isJsonNull() ? "null" : json.getAsJsonArray().get(0).getAsString();
            Optional<T> value = correction.tryCorrect(json.getAsJsonArray().get(1), mapper, ConfigCorrection.check(v -> v.size() == 1, v -> v.values().iterator().next()));
            return value.map(t -> ImmutableMap.of(key, t));
        } else if (json.isJsonArray() && json.getAsJsonArray().size() == 0) {
            return Optional.of(ImmutableMap.of());
        } else {
            return Optional.empty();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ConfigEditor<Map<String, T>> createEditor(ValueMapper<T, JsonElement> mapper, ValidatorInfo<?> validator) {
        return ConfigEditor.custom(Map.of(), map -> new MapContent<>(map, mapper.createEditor(ValidatorInfo.empty())));
    }
}

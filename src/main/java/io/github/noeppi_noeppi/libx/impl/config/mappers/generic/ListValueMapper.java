package io.github.noeppi_noeppi.libx.impl.config.mappers.generic;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.github.noeppi_noeppi.libx.config.GenericValueMapper;
import io.github.noeppi_noeppi.libx.config.ValidatorInfo;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import io.github.noeppi_noeppi.libx.config.correct.ConfigCorrection;
import io.github.noeppi_noeppi.libx.config.gui.ConfigEditor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Optional;

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
    public List<T> fromJson(JsonArray json, ValueMapper<T, JsonElement> mapper) {
        ImmutableList.Builder<T> builder = ImmutableList.builder();
        for (int i = 0; i < json.size(); i++) {
            JsonElement element = json.get(i);
            builder.add(mapper.fromJson(element));
        }
        return builder.build();
    }

    @Override
    public JsonArray toJson(List<T> value, ValueMapper<T, JsonElement> mapper) {
        JsonArray array = new JsonArray();
        for (T element : value) {
            array.add(mapper.toJson(element));
        }
        return array;
    }

    @Override
    public List<T> fromNetwork(FriendlyByteBuf buffer, ValueMapper<T, JsonElement> mapper) {
        int size = buffer.readVarInt();
        ImmutableList.Builder<T> builder = ImmutableList.builder();
        for (int i = 0; i < size; i++) {
            builder.add(mapper.fromNetwork(buffer));
        }
        return builder.build();
    }

    @Override
    public void toNetwork(List<T> value, FriendlyByteBuf buffer, ValueMapper<T, JsonElement> mapper) {
        buffer.writeVarInt(value.size());
        for (T element : value) {
            mapper.toNetwork(element, buffer);
        }
    }

    @Override
    public Optional<List<T>> correct(JsonElement json, ValueMapper<T, JsonElement> mapper, ConfigCorrection<List<T>> correction) {
        if (json.isJsonArray()) {
            // Keep everything that can load in the list
            ImmutableList.Builder<T> list = ImmutableList.builder();
            for (int i = 0; i < json.getAsJsonArray().size(); i++) {
                int idx = i;
                correction.tryCorrect(json.getAsJsonArray().get(i), mapper, ConfigCorrection.check(value -> idx < value.size(), value -> value.get(idx))).ifPresent(list::add);
            }
            List<T> result = list.build();
            if (result.isEmpty() && json.getAsJsonArray().size() > 0) {
                // Nothing matched. Might be better to return the default value here
                return Optional.empty();
            } else {
                return Optional.of(result);
            }
        } else {
            // Maybe someone forgot to add a list for a single item.
            // We just try to pass the entire json to the child mapper.
            return correction.tryCorrect(json, mapper, ConfigCorrection.check(value -> value.size() == 1, value -> value.get(0))).map(ImmutableList::of);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ConfigEditor<List<T>> createEditor(ValueMapper<T, JsonElement> mapper, ValidatorInfo<?> validator) {
        return ConfigEditor.unsupported(List.of());
    }
}

package io.github.noeppi_noeppi.libx.impl.config.mappers.special;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.lang3.tuple.Pair;

public class PairValueMapper<A, B> implements ValueMapper<Pair<A, B>, JsonArray> {

    private final ValueMapper<A, JsonElement> mapper1;
    private final ValueMapper<B, JsonElement> mapper2;

    public PairValueMapper(ValueMapper<A, JsonElement> mapper1, ValueMapper<B, JsonElement> mapper2) {
        this.mapper1 = mapper1;
        this.mapper2 = mapper2;
    }

    @Override
    public Class<Pair<A, B>> type() {
        //noinspection unchecked
        return (Class<Pair<A, B>>) (Class<?>) Pair.class;
    }

    @Override
    public Class<JsonArray> element() {
        return JsonArray.class;
    }

    @Override
    public Pair<A, B> fromJSON(JsonArray json) {
        if (json.size() != 2) {
            throw new IllegalStateException("Invalid list length for a pair: " + json.size());
        } else {
            return Pair.of(this.mapper1.fromJSON(json.get(0)), this.mapper2.fromJSON(json.get(1)));
        }
    }

    @Override
    public JsonArray toJSON(Pair<A, B> value) {
        JsonArray array = new JsonArray();
        array.add(this.mapper1.toJSON(value.getLeft()));
        array.add(this.mapper2.toJSON(value.getRight()));
        return array;
    }

    @Override
    public Pair<A, B> read(FriendlyByteBuf buffer) {
        return Pair.of(this.mapper1.read(buffer), this.mapper2.read(buffer));
    }

    @Override
    public void write(Pair<A, B> value, FriendlyByteBuf buffer) {
        this.mapper1.write(value.getLeft(), buffer);
        this.mapper2.write(value.getRight(), buffer);
    }
}

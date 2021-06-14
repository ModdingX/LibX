package io.github.noeppi_noeppi.libx.impl.config.mappers.special;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import net.minecraft.network.PacketBuffer;
import org.apache.commons.lang3.tuple.Triple;

public class TripleValueMapper<A, B, C> implements ValueMapper<Triple<A, B, C>, JsonArray> {

    private final ValueMapper<A, JsonElement> mapper1;
    private final ValueMapper<B, JsonElement> mapper2;
    private final ValueMapper<C, JsonElement> mapper3;

    public TripleValueMapper(ValueMapper<A, JsonElement> mapper1, ValueMapper<B, JsonElement> mapper2, ValueMapper<C, JsonElement> mapper3) {
        this.mapper1 = mapper1;
        this.mapper2 = mapper2;
        this.mapper3 = mapper3;
    }

    @Override
    public Class<Triple<A, B, C>> type() {
        //noinspection unchecked
        return (Class<Triple<A, B, C>>) (Class<?>) Triple.class;
    }

    @Override
    public Class<JsonArray> element() {
        return JsonArray.class;
    }

    @Override
    public Triple<A, B, C> fromJSON(JsonArray json) {
        if (json.size() != 3) {
            throw new IllegalStateException("Invalid list length for a triple: " + json.size());
        } else {
            return Triple.of(this.mapper1.fromJSON(json.get(0)), this.mapper2.fromJSON(json.get(1)), this.mapper3.fromJSON(json.get(2)));
        }
    }

    @Override
    public JsonArray toJSON(Triple<A, B, C> value) {
        JsonArray array = new JsonArray();
        array.add(this.mapper1.toJSON(value.getLeft()));
        array.add(this.mapper2.toJSON(value.getMiddle()));
        array.add(this.mapper3.toJSON(value.getRight()));
        return array;
    }

    @Override
    public Triple<A, B, C> read(PacketBuffer buffer) {
        return Triple.of(this.mapper1.read(buffer), this.mapper2.read(buffer), this.mapper3.read(buffer));
    }

    @Override
    public void write(Triple<A, B, C> value, PacketBuffer buffer) {
        this.mapper1.write(value.getLeft(), buffer);
        this.mapper2.write(value.getMiddle(), buffer);
        this.mapper3.write(value.getRight(), buffer);
    }
}
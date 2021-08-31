package io.github.noeppi_noeppi.libx.impl.config.mappers.special;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.github.noeppi_noeppi.libx.config.ValidatorInfo;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import io.github.noeppi_noeppi.libx.config.correct.ConfigCorrection;
import io.github.noeppi_noeppi.libx.config.gui.ConfigEditor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Optional;

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
    public Triple<A, B, C> fromJson(JsonArray json) {
        if (json.size() != 3) {
            throw new IllegalStateException("Invalid list length for a triple: " + json.size());
        } else {
            return Triple.of(this.mapper1.fromJson(json.get(0)), this.mapper2.fromJson(json.get(1)), this.mapper3.fromJson(json.get(2)));
        }
    }

    @Override
    public JsonArray toJson(Triple<A, B, C> value) {
        JsonArray array = new JsonArray();
        array.add(this.mapper1.toJson(value.getLeft()));
        array.add(this.mapper2.toJson(value.getMiddle()));
        array.add(this.mapper3.toJson(value.getRight()));
        return array;
    }

    @Override
    public Triple<A, B, C> fromNetwork(FriendlyByteBuf buffer) {
        return Triple.of(this.mapper1.fromNetwork(buffer), this.mapper2.fromNetwork(buffer), this.mapper3.fromNetwork(buffer));
    }

    @Override
    public void toNetwork(Triple<A, B, C> value, FriendlyByteBuf buffer) {
        this.mapper1.toNetwork(value.getLeft(), buffer);
        this.mapper2.toNetwork(value.getMiddle(), buffer);
        this.mapper3.toNetwork(value.getRight(), buffer);
    }

    @Override
    public Optional<Triple<A, B, C>> correct(JsonElement json, ConfigCorrection<Triple<A, B, C>> correction) {
        if (json.isJsonArray() && json.getAsJsonArray().size() == 3) {
            // We have a valid triple, it failed on the children. Correct all.
            Optional<A> first = correction.correct(json.getAsJsonArray().get(0), this.mapper1, Triple::getLeft);
            Optional<B> second = correction.correct(json.getAsJsonArray().get(1), this.mapper2, Triple::getMiddle);
            Optional<C> third = correction.correct(json.getAsJsonArray().get(2), this.mapper3, Triple::getRight);
            if (first.isPresent() && second.isPresent() && third.isPresent()) {
                return Optional.of(Triple.of(first.get(), second.get(), third.get()));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ConfigEditor<Triple<A, B, C>> createEditor(ValidatorInfo<?> validator) {
        return ConfigEditor.unsupported(Triple.of(
                this.mapper1.createEditor(null).defaultValue(),
                this.mapper2.createEditor(null).defaultValue(),
                this.mapper3.createEditor(null).defaultValue()
        ));
    }
}

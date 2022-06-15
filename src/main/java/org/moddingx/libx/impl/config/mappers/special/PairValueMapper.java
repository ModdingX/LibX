package org.moddingx.libx.impl.config.mappers.special;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
import org.moddingx.libx.config.correct.ConfigCorrection;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;
import org.moddingx.libx.impl.config.gui.editor.PairEditor;

import java.util.Map;
import java.util.Optional;

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
    public Pair<A, B> fromJson(JsonArray json) {
        if (json.size() != 2) {
            throw new IllegalStateException("Invalid list length for a pair: " + json.size());
        } else {
            return Pair.of(this.mapper1.fromJson(json.get(0)), this.mapper2.fromJson(json.get(1)));
        }
    }

    @Override
    public JsonArray toJson(Pair<A, B> value) {
        JsonArray array = new JsonArray();
        array.add(this.mapper1.toJson(value.getLeft()));
        array.add(this.mapper2.toJson(value.getRight()));
        return array;
    }

    @Override
    public Pair<A, B> fromNetwork(FriendlyByteBuf buffer) {
        return Pair.of(this.mapper1.fromNetwork(buffer), this.mapper2.fromNetwork(buffer));
    }

    @Override
    public void toNetwork(Pair<A, B> value, FriendlyByteBuf buffer) {
        this.mapper1.toNetwork(value.getLeft(), buffer);
        this.mapper2.toNetwork(value.getRight(), buffer);
    }

    @Override
    public Optional<Pair<A, B>> correct(JsonElement json, ConfigCorrection<Pair<A, B>> correction) {
        if (json.isJsonArray() && json.getAsJsonArray().size() == 2) {
            // We have a valid pair, it failed on the children. Correct both.
            Optional<A> first = correction.correct(json.getAsJsonArray().get(0), this.mapper1, Pair::getLeft);
            Optional<B> second = correction.correct(json.getAsJsonArray().get(1), this.mapper2, Pair::getRight);
            if (first.isPresent() && second.isPresent()) {
                return Optional.of(Pair.of(first.get(), second.get()));
            } else {
                return Optional.empty();
            }
        } else if (json.isJsonObject() && json.getAsJsonObject().size() == 2) {
            // Probably someone wrote a pair in an object
            Map.Entry<String, JsonElement> entry = json.getAsJsonObject().entrySet().iterator().next();
            Optional<A> first = correction.correct(new JsonPrimitive(entry.getKey()), this.mapper1, Pair::getLeft);
            Optional<B> second = correction.correct(entry.getValue(), this.mapper2, Pair::getRight);
            if (first.isPresent() && second.isPresent()) {
                return Optional.of(Pair.of(first.get(), second.get()));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ConfigEditor<Pair<A, B>> createEditor(ValidatorInfo<?> validator) {
        return new PairEditor<>(
                this.mapper1.createEditor(ValidatorInfo.empty()),
                this.mapper2.createEditor(ValidatorInfo.empty())
        );
    }
}

package org.moddingx.libx.impl.config.mappers.generic;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.moddingx.libx.codec.MoreStreamCodecs;
import org.moddingx.libx.config.correct.ConfigCorrection;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.mapper.GenericValueMapper;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;

import java.util.Optional;
import java.util.function.Function;

public class OptionValueMapper<T> implements GenericValueMapper<Optional<T>, JsonElement, T> {

    public static final OptionValueMapper<?> INSTANCE = new OptionValueMapper<>();

    private OptionValueMapper() {

    }

    @Override
    public Class<Optional<T>> type() {
        //noinspection unchecked
        return (Class<Optional<T>>) (Class<?>) Optional.class;
    }

    @Override
    public Class<JsonElement> element() {
        return JsonElement.class;
    }

    @Override
    public int getGenericElementPosition() {
        return 0;
    }

    @Override
    public Optional<T> fromJson(JsonElement json, ValueMapper<T, JsonElement> mapper) {
        if (json.isJsonNull()) {
            return Optional.empty();
        } else {
            return Optional.of(mapper.fromJson(json));
        }
    }

    @Override
    public JsonElement toJson(Optional<T> value, ValueMapper<T, JsonElement> mapper) {
        if (value.isEmpty()) {
            return JsonNull.INSTANCE;
        } else {
            return mapper.toJson(value.get());
        }
    }

    @Override
    public StreamCodec<? super FriendlyByteBuf, Optional<T>> streamCodec(ValueMapper<T, JsonElement> mapper) {
        return MoreStreamCodecs.maybe(mapper.streamCodec());
    }

    @Override
    public Optional<Optional<T>> correct(JsonElement json, ValueMapper<T, JsonElement> mapper, ConfigCorrection<Optional<T>> correction) {
        // We only ever need to correct values that are present.
        // null is filtered by fromJSON
        return correction.tryCorrect(json, mapper, Function.identity()).map(Optional::of);
    }

    @Override
    public ConfigEditor<Optional<T>> createEditor(ValueMapper<T, JsonElement> mapper, ValidatorInfo<?> validator) {
        return ConfigEditor.option(mapper.createEditor(ValidatorInfo.empty()));
    }
}

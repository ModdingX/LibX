package org.moddingx.libx.impl.config.mappers.generic;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
    public Optional<T> fromNetwork(FriendlyByteBuf buffer, ValueMapper<T, JsonElement> mapper) {
        if (!buffer.readBoolean()) {
            return Optional.empty();
        } else {
            return Optional.of(mapper.fromNetwork(buffer));
        }
    }

    @Override
    public void toNetwork(Optional<T> value, FriendlyByteBuf buffer, ValueMapper<T, JsonElement> mapper) {
        if (value.isEmpty()) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            mapper.toNetwork(value.get(), buffer);
        }
    }

    @Override
    public Optional<Optional<T>> correct(JsonElement json, ValueMapper<T, JsonElement> mapper, ConfigCorrection<Optional<T>> correction) {
        // We only ever need to correct values that are present.
        // null is filtered by fromJSON
        return correction.tryCorrect(json, mapper, Function.identity()).map(Optional::of);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ConfigEditor<Optional<T>> createEditor(ValueMapper<T, JsonElement> mapper, ValidatorInfo<?> validator) {
        return ConfigEditor.option(mapper.createEditor(ValidatorInfo.empty()));
    }
}

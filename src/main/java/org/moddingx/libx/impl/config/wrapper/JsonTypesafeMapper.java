package org.moddingx.libx.impl.config.wrapper;

import com.google.gson.JsonElement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.moddingx.libx.config.correct.ConfigCorrection;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;

import java.util.List;
import java.util.Optional;

public class JsonTypesafeMapper<C> implements ValueMapper<C, JsonElement> {

    protected final ValueMapper<C, ?> wrapped;

    public JsonTypesafeMapper(ValueMapper<C, ?> mapper) {
        this.wrapped = mapper;
    }

    @Override
    public Class<C> type() {
        return this.wrapped.type();
    }

    @Override
    public Class<JsonElement> element() {
        return JsonElement.class;
    }

    @Override
    public C fromJson(JsonElement json) {
        if (this.wrapped.element().isAssignableFrom(json.getClass())) {
            //noinspection unchecked
            return ((ValueMapper<C, JsonElement>) this.wrapped).fromJson(json);
        } else {
            throw new IllegalStateException("Json type mismatch: Expected " + this.wrapped.element() + ", got " + json.getClass());
        }
    }

    @Override
    public JsonElement toJson(C value) {
        return this.wrapped.toJson(value);
    }

    @Override
    public StreamCodec<? super FriendlyByteBuf, C> streamCodec() {
        return this.wrapped.streamCodec();
    }

    @Override
    public Optional<C> correct(JsonElement json, ConfigCorrection<C> correction) {
        return this.wrapped.correct(json, correction);
    }

    @Override
    public List<String> comment() {
        return this.wrapped.comment();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ConfigEditor<C> createEditor(ValidatorInfo<?> validator) {
        return this.wrapped.createEditor(validator);
    }
}

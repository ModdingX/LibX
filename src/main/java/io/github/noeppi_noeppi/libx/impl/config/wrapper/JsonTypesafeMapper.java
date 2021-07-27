package io.github.noeppi_noeppi.libx.impl.config.wrapper;

import com.google.gson.JsonElement;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import io.github.noeppi_noeppi.libx.config.correct.ConfigCorrection;
import net.minecraft.network.FriendlyByteBuf;

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
    public C fromNetwork(FriendlyByteBuf buffer) {
        return this.wrapped.fromNetwork(buffer);
    }

    @Override
    public void toNetwork(C value, FriendlyByteBuf buffer) {
        this.wrapped.toNetwork(value, buffer);
    }

    @Override
    public Optional<C> correct(JsonElement json, ConfigCorrection<C> correction) {
        return this.wrapped.correct(json, correction);
    }

    @Override
    public List<String> comment() {
        return this.wrapped.comment();
    }
}

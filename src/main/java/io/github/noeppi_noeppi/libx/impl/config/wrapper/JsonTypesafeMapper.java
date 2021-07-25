package io.github.noeppi_noeppi.libx.impl.config.wrapper;

import com.google.gson.JsonElement;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

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
    public C fromJSON(JsonElement json) {
        if (this.wrapped.element().isAssignableFrom(json.getClass())) {
            //noinspection unchecked
            return ((ValueMapper<C, JsonElement>) this.wrapped).fromJSON(json);
        } else {
            throw new IllegalStateException("Json type mismatch: Expected " + this.wrapped.element() + ", got " + json.getClass());
        }
    }

    @Override
    public JsonElement toJSON(C value) {
        return this.wrapped.toJSON(value);
    }

    @Override
    public C read(FriendlyByteBuf buffer) {
        return this.wrapped.read(buffer);
    }

    @Override
    public void write(C value, FriendlyByteBuf buffer) {
        this.wrapped.write(value, buffer);
    }

    @Override
    public List<String> comment() {
        return this.wrapped.comment();
    }
}

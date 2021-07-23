package io.github.noeppi_noeppi.libx.impl.config.wrapper;

import com.google.gson.JsonElement;
import io.github.noeppi_noeppi.libx.config.GenericValueMapper;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public class WrappedGenericMapper<T, E extends JsonElement, C> implements ValueMapper<T, E> {

    private final GenericValueMapper<T, E, C> parent;
    private final ValueMapper<C, JsonElement> mapper;

    public WrappedGenericMapper(GenericValueMapper<T, E, C> parent, ValueMapper<C, JsonElement> mapper) {
        this.parent = parent;
        this.mapper = mapper;
    }

    @Override
    public Class<T> type() {
        return this.parent.type();
    }

    @Override
    public Class<E> element() {
        return this.parent.element();
    }

    @Override
    public T fromJSON(E json) {
        return this.parent.fromJSON(json, this.mapper);
    }

    @Override
    public E toJSON(T value) {
        return this.parent.toJSON(value, this.mapper);
    }

    @Override
    public T read(FriendlyByteBuf buffer) {
        return this.parent.read(buffer, this.mapper);
    }

    @Override
    public void write(T value, FriendlyByteBuf buffer) {
        this.parent.write(value, buffer, this.mapper);
    }

    @Override
    public List<String> comment() {
        return this.parent.comment();
    }
}

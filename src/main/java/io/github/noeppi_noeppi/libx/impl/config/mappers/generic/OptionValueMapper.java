package io.github.noeppi_noeppi.libx.impl.config.mappers.generic;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import io.github.noeppi_noeppi.libx.config.GenericValueMapper;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import net.minecraft.network.PacketBuffer;

import java.util.Optional;

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
    public Optional<T> fromJSON(JsonElement json, ValueMapper<T, JsonElement> mapper) {
        if (json.isJsonNull()) {
            return Optional.empty();
        } else {
            return Optional.of(mapper.fromJSON(json));
        }
    }

    @Override
    public JsonElement toJSON(Optional<T> value, ValueMapper<T, JsonElement> mapper) {
        if (!value.isPresent()) {
            return JsonNull.INSTANCE;
        } else {
            return mapper.toJSON(value.get());
        }
    }

    @Override
    public Optional<T> read(PacketBuffer buffer, ValueMapper<T, JsonElement> mapper) {
        if (!buffer.readBoolean()) {
            return Optional.empty();
        } else {
            return Optional.of(mapper.read(buffer));
        }
    }

    @Override
    public void write(Optional<T> value, PacketBuffer buffer, ValueMapper<T, JsonElement> mapper) {
        if (!value.isPresent()) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            mapper.write(value.get(), buffer);
        }
    }
}

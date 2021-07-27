package io.github.noeppi_noeppi.libx.impl.config.mappers.advanced;

import com.google.gson.JsonPrimitive;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public class UidValueMapper implements ValueMapper<UUID, JsonPrimitive> {
    
    public static final UidValueMapper INSTANCE = new UidValueMapper();

    private UidValueMapper() {

    }

    @Override
    public Class<java.util.UUID> type() {
        return UUID.class;
    }

    @Override
    public Class<JsonPrimitive> element() {
        return JsonPrimitive.class;
    }

    @Override
    public java.util.UUID fromJson(JsonPrimitive json) {
        return UUID.fromString(json.getAsString());
    }

    @Override
    public JsonPrimitive toJson(java.util.UUID value) {
        return new JsonPrimitive(value.toString());
    }

    @Override
    public UUID fromNetwork(FriendlyByteBuf buffer) {
        long mostSignificantBits = buffer.readLong();
        long leastSignificantBits = buffer.readLong();
        return new UUID(mostSignificantBits, leastSignificantBits);
    }

    @Override
    public void toNetwork(UUID value, FriendlyByteBuf buffer) {
        buffer.writeLong(value.getMostSignificantBits());
        buffer.writeLong(value.getLeastSignificantBits());
    }
}

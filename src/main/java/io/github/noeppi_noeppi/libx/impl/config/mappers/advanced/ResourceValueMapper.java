package io.github.noeppi_noeppi.libx.impl.config.mappers.advanced;

import com.google.gson.JsonPrimitive;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ResourceValueMapper implements ValueMapper<ResourceLocation, JsonPrimitive> {

    public static final ResourceValueMapper INSTANCE = new ResourceValueMapper();

    private ResourceValueMapper() {

    }

    @Override
    public Class<ResourceLocation> type() {
        return ResourceLocation.class;
    }

    @Override
    public Class<JsonPrimitive> element() {
        return JsonPrimitive.class;
    }

    @Override
    public ResourceLocation fromJSON(JsonPrimitive json) {
        return new ResourceLocation(json.getAsString());
    }

    @Override
    public JsonPrimitive toJSON(ResourceLocation value) {
        return new JsonPrimitive(value.toString());
    }

    @Override
    public ResourceLocation read(FriendlyByteBuf buffer) {
        return buffer.readResourceLocation();
    }

    @Override
    public void write(ResourceLocation value, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(value);
    }
}

package io.github.noeppi_noeppi.libx.impl.config.mappers.advanced;

import com.google.gson.JsonObject;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import io.github.noeppi_noeppi.libx.util.ResourceList;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public class ResourceListValueMapper implements ValueMapper<ResourceList, JsonObject> {

    public static final ResourceListValueMapper INSTANCE = new ResourceListValueMapper();

    private ResourceListValueMapper() {

    }

    private final List<String> COMMENT = List.of("This is a resource list. See https://noeppi-noeppi.github.io/LibX/io/github/noeppi_noeppi/libx/util/ResourceList.html#use_resource_lists_in_configs");

    @Override
    public Class<ResourceList> type() {
        return ResourceList.class;
    }

    @Override
    public Class<JsonObject> element() {
        return JsonObject.class;
    }

    @Override
    public ResourceList fromJson(JsonObject json) {
        return new ResourceList(json);
    }

    @Override
    public JsonObject toJson(ResourceList value) {
        return value.toJson();
    }

    @Override
    public ResourceList fromNetwork(FriendlyByteBuf buffer) {
        return new ResourceList(buffer);
    }

    @Override
    public void toNetwork(ResourceList value, FriendlyByteBuf buffer) {
        value.toNetwork(buffer);
    }

    @Override
    public List<String> comment() {
        return this.COMMENT;
    }
}

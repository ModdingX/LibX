package org.moddingx.libx.impl.config.mappers.advanced;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;
import org.moddingx.libx.impl.config.gui.screen.content.ResourceListContent;
import org.moddingx.libx.util.data.ResourceList;

import java.net.URI;
import java.util.List;

public class ResourceListValueMapper implements ValueMapper<ResourceList, JsonObject> {

    public static final ResourceListValueMapper INSTANCE = new ResourceListValueMapper();
    
    public static final URI INFO_URL = URI.create("https://moddingx.org/libx/org/moddingx/libx/util/data/ResourceList.html#use_resource_lists_in_configs");

    private static final List<String> COMMENT = List.of("This is a resource list. See " + INFO_URL);

    private ResourceListValueMapper() {

    }

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
    public StreamCodec<? super FriendlyByteBuf, ResourceList> streamCodec() {
        return ResourceList.STREAM_CODEC;
    }

    @Override
    public List<String> comment() {
        return COMMENT;
    }

    @Override
    public ConfigEditor<ResourceList> createEditor(ValidatorInfo<?> validator) {
        return ConfigEditor.custom(ResourceList.ALLOW_LIST, ResourceListContent::new);
    }
}

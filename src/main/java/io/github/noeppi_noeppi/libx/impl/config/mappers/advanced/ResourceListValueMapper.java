package io.github.noeppi_noeppi.libx.impl.config.mappers.advanced;

import com.google.gson.JsonObject;
import io.github.noeppi_noeppi.libx.config.ValidatorInfo;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import io.github.noeppi_noeppi.libx.config.gui.ConfigEditor;
import io.github.noeppi_noeppi.libx.impl.config.gui.screen.content.ResourceListContent;
import io.github.noeppi_noeppi.libx.util.ResourceList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class ResourceListValueMapper implements ValueMapper<ResourceList, JsonObject> {

    public static final ResourceListValueMapper INSTANCE = new ResourceListValueMapper();
    
    public static final URL INFO_URL;

    static {
        try {
            INFO_URL = new URL("https://noeppi-noeppi.github.io/LibX/io/github/noeppi_noeppi/libx/util/ResourceList.html#use_resource_lists_in_configs");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

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
    public ResourceList fromNetwork(FriendlyByteBuf buffer) {
        return new ResourceList(buffer);
    }

    @Override
    public void toNetwork(ResourceList value, FriendlyByteBuf buffer) {
        value.toNetwork(buffer);
    }

    @Override
    public List<String> comment() {
        return COMMENT;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ConfigEditor<ResourceList> createEditor(ValidatorInfo<?> validator) {
        return ConfigEditor.custom(ResourceList.WHITELIST, ResourceListContent::new);
    }
}

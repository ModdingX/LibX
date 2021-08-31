package io.github.noeppi_noeppi.libx.impl.config.mappers.advanced;

import com.google.gson.JsonPrimitive;
import io.github.noeppi_noeppi.libx.config.ValidatorInfo;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import io.github.noeppi_noeppi.libx.config.gui.ConfigEditor;
import io.github.noeppi_noeppi.libx.util.Misc;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
    public ResourceLocation fromJson(JsonPrimitive json) {
        return new ResourceLocation(json.getAsString());
    }

    @Override
    public JsonPrimitive toJson(ResourceLocation value) {
        return new JsonPrimitive(value.toString());
    }

    @Override
    public ResourceLocation fromNetwork(FriendlyByteBuf buffer) {
        return buffer.readResourceLocation();
    }

    @Override
    public void toNetwork(ResourceLocation value, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(value);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ConfigEditor<ResourceLocation> createEditor(ValidatorInfo<?> validator) {
        return ConfigEditor.unsupported(Misc.MISSIGNO);
    }
}

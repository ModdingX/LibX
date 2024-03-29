package org.moddingx.libx.impl.config.mappers.advanced;

import com.google.gson.JsonPrimitive;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.InputProperties;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;
import org.moddingx.libx.util.Misc;

public class ResourceValueMapper implements ValueMapper<ResourceLocation, JsonPrimitive> {

    public static final ResourceValueMapper INSTANCE = new ResourceValueMapper();
    private static final InputProperties<ResourceLocation> INPUT = new InputProperties<>() {

        @Override
        public ResourceLocation defaultValue() {
            return Misc.MISSINGNO;
        }

        @Override
        public boolean canInputChar(char chr) {
            return ResourceLocation.isAllowedInResourceLocation(chr);
        }

        @Override
        public boolean isValid(String str) {
            return ResourceLocation.tryParse(str) != null;
        }

        @Override
        public ResourceLocation valueOf(String str) {
            return new ResourceLocation(str);
        }
    };

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
        return ConfigEditor.input(INPUT, validator);
    }
}

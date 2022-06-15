package org.moddingx.libx.impl.config.mappers.advanced;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.moddingx.libx.config.validator.ValidatorInfo;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.crafting.IngredientStack;

public class IngredientStackValueMapper implements ValueMapper<IngredientStack, JsonObject> {

    public static final IngredientStackValueMapper INSTANCE = new IngredientStackValueMapper();

    private IngredientStackValueMapper() {

    }

    @Override
    public Class<IngredientStack> type() {
        return IngredientStack.class;
    }

    @Override
    public Class<JsonObject> element() {
        return JsonObject.class;
    }

    @Override
    public IngredientStack fromJson(JsonObject json) {
        return IngredientStack.fromJson(json);
    }

    @Override
    public JsonObject toJson(IngredientStack value) {
        return value.toJson();
    }

    @Override
    public IngredientStack fromNetwork(FriendlyByteBuf buffer) {
        return IngredientStack.fromNetwork(buffer);
    }

    @Override
    public void toNetwork(IngredientStack value, FriendlyByteBuf buffer) {
        value.toNetwork(buffer);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ConfigEditor<IngredientStack> createEditor(ValidatorInfo<?> validator) {
        return ConfigEditor.unsupported(IngredientStack.EMPTY);
    }
}

package org.moddingx.libx.impl.config.mappers.advanced;

import com.google.gson.JsonElement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.moddingx.libx.config.ValidatorInfo;
import org.moddingx.libx.config.ValueMapper;
import org.moddingx.libx.config.gui.ConfigEditor;

public class IngredientValueMapper implements ValueMapper<Ingredient, JsonElement> {

    public static final IngredientValueMapper INSTANCE = new IngredientValueMapper();

    private IngredientValueMapper() {

    }

    @Override
    public Class<Ingredient> type() {
        return Ingredient.class;
    }

    @Override
    public Class<JsonElement> element() {
        return JsonElement.class;
    }

    @Override
    public Ingredient fromJson(JsonElement json) {
        return Ingredient.fromJson(json);
    }

    @Override
    public JsonElement toJson(Ingredient value) {
        return value.toJson();
    }

    @Override
    public Ingredient fromNetwork(FriendlyByteBuf buffer) {
        return Ingredient.fromNetwork(buffer);
    }

    @Override
    public void toNetwork(Ingredient value, FriendlyByteBuf buffer) {
        value.toNetwork(buffer);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ConfigEditor<Ingredient> createEditor(ValidatorInfo<?> validator) {
        return ConfigEditor.unsupported(Ingredient.EMPTY);
    }
}

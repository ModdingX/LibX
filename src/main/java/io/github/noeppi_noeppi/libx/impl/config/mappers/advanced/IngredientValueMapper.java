package io.github.noeppi_noeppi.libx.impl.config.mappers.advanced;

import com.google.gson.JsonElement;
import io.github.noeppi_noeppi.libx.config.ValidatorInfo;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import io.github.noeppi_noeppi.libx.config.gui.ConfigEditor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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

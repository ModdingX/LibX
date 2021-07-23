package io.github.noeppi_noeppi.libx.impl.config.mappers.advanced;

import com.google.gson.JsonElement;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.network.FriendlyByteBuf;

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
    public Ingredient fromJSON(JsonElement json) {
        return Ingredient.fromJson(json);
    }

    @Override
    public JsonElement toJSON(Ingredient value) {
        return value.toJson();
    }

    @Override
    public Ingredient read(FriendlyByteBuf buffer) {
        return Ingredient.fromNetwork(buffer);
    }

    @Override
    public void write(Ingredient value, FriendlyByteBuf buffer) {
        value.toNetwork(buffer);
    }
}

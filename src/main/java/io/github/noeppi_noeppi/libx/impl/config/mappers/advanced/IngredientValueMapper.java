package io.github.noeppi_noeppi.libx.impl.config.mappers.advanced;

import com.google.gson.JsonElement;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;

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
        return Ingredient.deserialize(json);
    }

    @Override
    public JsonElement toJSON(Ingredient value) {
        return value.serialize();
    }

    @Override
    public Ingredient read(PacketBuffer buffer) {
        return Ingredient.read(buffer);
    }

    @Override
    public void write(Ingredient value, PacketBuffer buffer) {
        value.write(buffer);
    }
}

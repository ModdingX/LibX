package io.github.noeppi_noeppi.libx.impl.config.mappers.advanced;

import com.google.gson.JsonObject;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import io.github.noeppi_noeppi.libx.crafting.IngredientStack;
import net.minecraft.network.FriendlyByteBuf;

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
    public IngredientStack fromJSON(JsonObject json) {
        return IngredientStack.fromJson(json);
    }

    @Override
    public JsonObject toJSON(IngredientStack value) {
        return value.toJson();
    }

    @Override
    public IngredientStack read(FriendlyByteBuf buffer) {
        return IngredientStack.fromNetwork(buffer);
    }

    @Override
    public void write(IngredientStack value, FriendlyByteBuf buffer) {
        value.toNetwork(buffer);
    }
}

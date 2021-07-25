package io.github.noeppi_noeppi.libx.crafting;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Predicate;

/**
 * An {@link Ingredient} with an amount.
 */
public record IngredientStack(Ingredient ingredient, int count) implements Predicate<ItemStack> {

    /**
     * Returns whether the ingredient matches the stack and the count of the stack is greater or equal
     * to the count of the IngredientStack.
     */
    @Override
    public boolean test(ItemStack stack) {
        return stack.getCount() >= this.count && this.ingredient.test(stack);
    }

    /**
     * Returns whether the count is 0 or Ingredient#hasNoMatchingItems return true.
     */
    public boolean empty() {
        return this.count == 0 || this.ingredient.isEmpty();
    }

    /**
     * Serialises the IngredientStack to json.
     */
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.add("Ingredient", this.ingredient.toJson());
        json.addProperty("Count", this.count);
        return json;
    }

    /**
     * Writes this IngredientStack to a {@link FriendlyByteBuf}
     */
    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.count);
        this.ingredient.toNetwork(buffer);
    }

    /**
     * Deserializes and IngredientStack from json.
     */
    public static IngredientStack deserialize(JsonObject json) {
        Ingredient ingredient = json.has("Ingredient") ? Ingredient.fromJson(json.get("Ingredient")) : Ingredient.EMPTY;
        int count = json.has("Count") && json.get("Count").isJsonPrimitive() ? json.get("Count").getAsInt() : 0;
        return new IngredientStack(ingredient, count);
    }

    /**
     * Reads an IngredientStack from a {@link FriendlyByteBuf}
     */
    public static IngredientStack read(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        Ingredient ingredient = Ingredient.fromNetwork(buffer);
        return new IngredientStack(ingredient, count);
    }
}

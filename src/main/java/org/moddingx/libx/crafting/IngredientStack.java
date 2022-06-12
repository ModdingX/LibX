package org.moddingx.libx.crafting;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Predicate;

/**
 * An {@link Ingredient} with an amount.
 */
public record IngredientStack(Ingredient ingredient, int count) implements Predicate<ItemStack> {

    public static final IngredientStack EMPTY = new IngredientStack(Ingredient.EMPTY, 1);
    
    /**
     * Returns whether the ingredient matches the stack and the count of the stack is greater or equal
     * to the count of the IngredientStack.
     */
    @Override
    public boolean test(ItemStack stack) {
        return stack.getCount() >= this.count && this.ingredient.test(stack);
    }
    
    /**
     * Returns whether the count is 0 or {@link Ingredient#isEmpty()} return true.
     */
    public boolean isEmpty() {
        return this.count == 0 || this.ingredient.isEmpty();
    }

    /**
     * Serialises the IngredientStack to json.
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.add("Ingredient", this.ingredient.toJson());
        json.addProperty("Count", this.count);
        return json;
    }

    /**
     * Writes this IngredientStack to a {@link FriendlyByteBuf}.
     */
    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.count);
        this.ingredient.toNetwork(buffer);
    }

    /**
     * Deserializes and IngredientStack from json.
     */
    public static IngredientStack fromJson(JsonObject json) {
        Ingredient ingredient = json.has("Ingredient") ? Ingredient.fromJson(json.get("Ingredient")) : Ingredient.EMPTY;
        int count = json.has("Count") && json.get("Count").isJsonPrimitive() ? json.get("Count").getAsInt() : 0;
        return new IngredientStack(ingredient, count);
    }

    /**
     * Reads an IngredientStack from a {@link FriendlyByteBuf}.
     */
    public static IngredientStack fromNetwork(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        Ingredient ingredient = Ingredient.fromNetwork(buffer);
        return new IngredientStack(ingredient, count);
    }
}

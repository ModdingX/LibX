package io.github.noeppi_noeppi.libx.crafting;

import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Predicate;

/**
 * An {@link Ingredient} with an amount.
 */
public class IngredientStack implements Predicate<ItemStack> {
    
    private final Ingredient ingredient;
    private final int count;

    /**
     * Creates a new IngredientStack
     */
    public IngredientStack(Ingredient ingredient, int count) {
        this.ingredient = ingredient;
        this.count = Math.max(count, 0);
    }

    /**
     * Gets the {@link Ingredient} for this ingredient stack.
     */
    public Ingredient getIngredient() {
        return this.ingredient;
    }

    /**
     * Gets the count for this ingredient stack.
     */
    public int getCount() {
        return this.count;
    }

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
    public boolean isEmpty() {
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
     * Writes this IngredientStack to a {@link PacketBuffer}
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
     * Reads an IngredientStack from a {@link PacketBuffer}
     */
    public static IngredientStack read(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        Ingredient ingredient = Ingredient.fromNetwork(buffer);
        return new IngredientStack(ingredient, count);
    }
}

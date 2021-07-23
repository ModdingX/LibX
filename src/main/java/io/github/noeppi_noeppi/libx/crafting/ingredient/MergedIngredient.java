package io.github.noeppi_noeppi.libx.crafting.ingredient;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Vanilla provides a way to merge vanilla ingredients. However this does not work with modded ingredients.
 * So this provides a method to merge multiple ingredients that also works with mod ingredients.
 */
public class MergedIngredient extends Ingredient {

    private final List<Ingredient> ingredients;
    
    protected MergedIngredient(List<Ingredient> ingredients) {
        super(Stream.empty());
        this.ingredients = ImmutableList.copyOf(ingredients);
    }
    
    /**
     * Merges some ingredients together. If only vanilla ingredients are used, this will use vanilla merging.
     */
    public static Ingredient mergeIngredients(Ingredient... ingredients) {
        return mergeIngredients(Arrays.asList(ingredients));
    }

    /**
     * Merges some ingredients together. If only vanilla ingredients are used, this will use vanilla merging.
     */
    public static Ingredient mergeIngredients(List<Ingredient> ingredients) {
        if (ingredients.isEmpty()) {
            return Ingredient.EMPTY;
        } else if (ingredients.size() == 1) {
            return ingredients.get(0);
        } else if (ingredients.stream().allMatch(Ingredient::isVanilla)) {
            // Only vanilla ingredients, we can use vanilla merging
            return Ingredient.fromValues(ingredients.stream()
                    .flatMap(i -> Arrays.stream(i.values))
            );
        } else if (ingredients.stream().anyMatch(Ingredient::isVanilla)) {
            // We ave at least some vanilla ingredients.
            // Merge them first
            Ingredient vanilla = Ingredient.fromValues(ingredients.stream()
                    .filter(Ingredient::isVanilla)
                    .flatMap(i -> Arrays.stream(i.values))
            );
            List<Ingredient> list = new ArrayList<>();
            list.add(vanilla);
            ingredients.stream()
                    .filter(i -> !i.isVanilla())
                    .forEach(list::add);
            return new MergedIngredient(list);
        } else {
            return new MergedIngredient(ingredients);
        }
    }

    public List<Ingredient> getIngredients() {
        return this.ingredients;
    }

    @Nonnull
    @Override
    public ItemStack[] getItems() {
        return this.ingredients.stream()
                .flatMap(i -> Arrays.stream(i.getItems()))
                .toArray(ItemStack[]::new);
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        return this.ingredients.stream().anyMatch(i -> i.test(stack));
    }

    @Nonnull
    @Override
    public IntList getStackingIds() {
        IntArrayList ial = new IntArrayList();
        for (Ingredient i : this.ingredients) {
            ial.addAll(i.getStackingIds());
        }
        return ial;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Nonnull
    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return MergedIngredient.Serializer.INSTANCE;
    }

    @Override
    public boolean isEmpty() {
        return this.ingredients.stream().allMatch(Ingredient::isEmpty);
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions")
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", CraftingHelper.getID(MergedIngredient.Serializer.INSTANCE).toString());
        JsonArray array = new JsonArray();
        for (Ingredient i : this.ingredients) {
            array.add(i.toJson());
        }
        json.add("ingredients", array);
        return json;
    }

    public static class Serializer implements IIngredientSerializer<MergedIngredient> {

        public static final MergedIngredient.Serializer INSTANCE = new MergedIngredient.Serializer();

        private Serializer() {

        }

        @Nonnull
        @Override
        public MergedIngredient parse(@Nonnull FriendlyByteBuf buffer) {
            int size = buffer.readVarInt();
            List<Ingredient> list = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                list.add(Ingredient.fromNetwork(buffer));
            }
            return new MergedIngredient(list);
        }

        @Nonnull
        @Override
        public MergedIngredient parse(JsonObject json) {
            JsonArray array = json.get("ingredients").getAsJsonArray();
            List<Ingredient> list = new ArrayList<>();
            for (JsonElement elem : array) {
                list.add(Ingredient.fromJson(elem));
            }
            return new MergedIngredient(list);
        }

        @Override
        public void write(@Nonnull FriendlyByteBuf buffer, @Nonnull MergedIngredient ingredient) {
            buffer.writeVarInt(ingredient.ingredients.size());
            for (Ingredient i : ingredient.ingredients) {
                i.toNetwork(buffer);
            }
        }
    }
}

package io.github.noeppi_noeppi.libx.data.provider.recipe;

import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.UpgradeRecipeBuilder;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.List;

/**
 * A {@link RecipeExtension} for recipes in the smithing table..
 */
public interface SmithingExtension extends RecipeExtension {

    /**
     * Adds  a smithing recipe with the given inputs and output.
     */
    default void smithing(ItemLike base, ItemLike addition, ItemLike result) {
        this.smithing(Ingredient.of(base), Ingredient.of(addition), result);
    }

    /**
     * Adds  a smithing recipe with the given inputs and output.
     */
    default void smithing(ItemLike base, Tag<Item> addition, ItemLike result) {
        this.smithing(Ingredient.of(base), Ingredient.of(addition), result);
    }

    /**
     * Adds  a smithing recipe with the given inputs and output.
     */
    default void smithing(ItemLike base, Ingredient addition, ItemLike result) {
        this.smithing(Ingredient.of(base), addition, result);
    }

    /**
     * Adds  a smithing recipe with the given inputs and output.
     */
    default void smithing(Tag<Item> base, ItemLike addition, ItemLike result) {
        this.smithing(Ingredient.of(base), Ingredient.of(addition), result);
    }

    /**
     * Adds  a smithing recipe with the given inputs and output.
     */
    default void smithing(Tag<Item> base, Tag<Item> addition, ItemLike result) {
        this.smithing(Ingredient.of(base), Ingredient.of(addition), result);
    }

    /**
     * Adds  a smithing recipe with the given inputs and output.
     */
    default void smithing(Tag<Item> base, Ingredient addition, ItemLike result) {
        this.smithing(Ingredient.of(base), addition, result);
    }

    /**
     * Adds  a smithing recipe with the given inputs and output.
     */
    default void smithing(Ingredient base, ItemLike addition, ItemLike result) {
        this.smithing(base, Ingredient.of(addition), result);
    }

    /**
     * Adds  a smithing recipe with the given inputs and output.
     */
    default void smithing(Ingredient base, Tag<Item> addition, ItemLike result) {
        this.smithing(base, Ingredient.of(addition), result);
    }

    /**
     * Adds  a smithing recipe with the given inputs and output.
     */
    default void smithing(Ingredient base, Ingredient addition, ItemLike result) {
        UpgradeRecipeBuilder builder = UpgradeRecipeBuilder.smithing(base, addition, result.asItem());
        List<CriterionTriggerInstance> criteria = this.criteria(base);
        for (int i = 0; i < criteria.size(); i++) {
            builder.unlocks("has_item" + i, criteria.get(i));
        }
        builder.save(this.consumer(), this.provider().loc(result, "smithing"));
    }
}

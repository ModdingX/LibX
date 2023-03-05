package org.moddingx.libx.datagen.provider.recipe;

import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.UpgradeRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.List;

/**
 * A {@link RecipeExtension} for recipes in the smithing table..
 */
public interface SmithingExtension extends RecipeExtension {

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(ItemLike base, ItemLike addition, ItemLike result) {
        this.smithing(RecipeCategory.MISC, base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(ItemLike base, TagKey<Item> addition, ItemLike result) {
        this.smithing(RecipeCategory.MISC, base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(ItemLike base, Ingredient addition, ItemLike result) {
        this.smithing(RecipeCategory.MISC, base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(TagKey<Item> base, ItemLike addition, ItemLike result) {
        this.smithing(RecipeCategory.MISC, base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(TagKey<Item> base, TagKey<Item> addition, ItemLike result) {
        this.smithing(RecipeCategory.MISC, base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(TagKey<Item> base, Ingredient addition, ItemLike result) {
        this.smithing(RecipeCategory.MISC, base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(Ingredient base, ItemLike addition, ItemLike result) {
        this.smithing(RecipeCategory.MISC, base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(Ingredient base, TagKey<Item> addition, ItemLike result) {
        this.smithing(RecipeCategory.MISC, base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(Ingredient base, Ingredient addition, ItemLike result) {
        this.smithing(RecipeCategory.MISC, base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, ItemLike base, ItemLike addition, ItemLike result) {
        this.smithing(category, Ingredient.of(base), Ingredient.of(addition), result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, ItemLike base, TagKey<Item> addition, ItemLike result) {
        this.smithing(category, Ingredient.of(base), Ingredient.of(addition), result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, ItemLike base, Ingredient addition, ItemLike result) {
        this.smithing(category, Ingredient.of(base), addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, TagKey<Item> base, ItemLike addition, ItemLike result) {
        this.smithing(category, Ingredient.of(base), Ingredient.of(addition), result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, TagKey<Item> base, TagKey<Item> addition, ItemLike result) {
        this.smithing(category, Ingredient.of(base), Ingredient.of(addition), result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, TagKey<Item> base, Ingredient addition, ItemLike result) {
        this.smithing(category, Ingredient.of(base), addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, Ingredient base, ItemLike addition, ItemLike result) {
        this.smithing(category, base, Ingredient.of(addition), result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, Ingredient base, TagKey<Item> addition, ItemLike result) {
        this.smithing(category, base, Ingredient.of(addition), result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, Ingredient base, Ingredient addition, ItemLike result) {
        UpgradeRecipeBuilder builder = UpgradeRecipeBuilder.smithing(base, addition, category, result.asItem());
        List<CriterionTriggerInstance> criteria = this.criteria(base);
        for (int i = 0; i < criteria.size(); i++) {
            builder.unlocks("has_item" + i, criteria.get(i));
        }
        builder.save(this.consumer(), this.provider().loc(result, "smithing"));
    }
}

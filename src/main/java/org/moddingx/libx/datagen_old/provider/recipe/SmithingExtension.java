package org.moddingx.libx.datagen_old.provider.recipe;

import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
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
    default void smithing(ItemLike template, ItemLike base, ItemLike addition, ItemLike result) {
        this.smithing(Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(ItemLike template, ItemLike base, TagKey<Item> addition, ItemLike result) {
        this.smithing(Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(ItemLike template, ItemLike base, Ingredient addition, ItemLike result) {
        this.smithing(Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(ItemLike template, TagKey<Item> base, ItemLike addition, ItemLike result) {
        this.smithing(Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(ItemLike template, TagKey<Item> base, TagKey<Item> addition, ItemLike result) {
        this.smithing(Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(ItemLike template, TagKey<Item> base, Ingredient addition, ItemLike result) {
        this.smithing(Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(ItemLike template, Ingredient base, ItemLike addition, ItemLike result) {
        this.smithing(Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(ItemLike template, Ingredient base, TagKey<Item> addition, ItemLike result) {
        this.smithing(Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(ItemLike template, Ingredient base, Ingredient addition, ItemLike result) {
        this.smithing(Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(TagKey<Item> template, ItemLike base, ItemLike addition, ItemLike result) {
        this.smithing(Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(TagKey<Item> template, ItemLike base, TagKey<Item> addition, ItemLike result) {
        this.smithing(Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(TagKey<Item> template, ItemLike base, Ingredient addition, ItemLike result) {
        this.smithing(Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(TagKey<Item> template, TagKey<Item> base, ItemLike addition, ItemLike result) {
        this.smithing(Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(TagKey<Item> template, TagKey<Item> base, TagKey<Item> addition, ItemLike result) {
        this.smithing(Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(TagKey<Item> template, TagKey<Item> base, Ingredient addition, ItemLike result) {
        this.smithing(Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(TagKey<Item> template, Ingredient base, ItemLike addition, ItemLike result) {
        this.smithing(Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(TagKey<Item> template, Ingredient base, TagKey<Item> addition, ItemLike result) {
        this.smithing(Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(TagKey<Item> template, Ingredient base, Ingredient addition, ItemLike result) {
        this.smithing(Ingredient.of(template), base, addition, result);
    }
    
    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(Ingredient template, ItemLike base, ItemLike addition, ItemLike result) {
        this.smithing(RecipeCategory.MISC, template, base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(Ingredient template, ItemLike base, TagKey<Item> addition, ItemLike result) {
        this.smithing(RecipeCategory.MISC, template, base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(Ingredient template, ItemLike base, Ingredient addition, ItemLike result) {
        this.smithing(RecipeCategory.MISC, template, base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(Ingredient template, TagKey<Item> base, ItemLike addition, ItemLike result) {
        this.smithing(RecipeCategory.MISC, template, base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(Ingredient template, TagKey<Item> base, TagKey<Item> addition, ItemLike result) {
        this.smithing(RecipeCategory.MISC, template, base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(Ingredient template, TagKey<Item> base, Ingredient addition, ItemLike result) {
        this.smithing(RecipeCategory.MISC, template, base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(Ingredient template, Ingredient base, ItemLike addition, ItemLike result) {
        this.smithing(RecipeCategory.MISC, template, base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(Ingredient template, Ingredient base, TagKey<Item> addition, ItemLike result) {
        this.smithing(RecipeCategory.MISC, template, base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(Ingredient template, Ingredient base, Ingredient addition, ItemLike result) {
        this.smithing(RecipeCategory.MISC, template, base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, ItemLike template, ItemLike base, ItemLike addition, ItemLike result) {
        this.smithing(category, Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, ItemLike template, ItemLike base, TagKey<Item> addition, ItemLike result) {
        this.smithing(category, Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, ItemLike template, ItemLike base, Ingredient addition, ItemLike result) {
        this.smithing(category, Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, ItemLike template, TagKey<Item> base, ItemLike addition, ItemLike result) {
        this.smithing(category, Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, ItemLike template, TagKey<Item> base, TagKey<Item> addition, ItemLike result) {
        this.smithing(category, Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, ItemLike template, TagKey<Item> base, Ingredient addition, ItemLike result) {
        this.smithing(category, Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, ItemLike template, Ingredient base, ItemLike addition, ItemLike result) {
        this.smithing(category, Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, ItemLike template, Ingredient base, TagKey<Item> addition, ItemLike result) {
        this.smithing(category, Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, ItemLike template, Ingredient base, Ingredient addition, ItemLike result) {
        this.smithing(category, Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, TagKey<Item> template, ItemLike base, ItemLike addition, ItemLike result) {
        this.smithing(category, Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, TagKey<Item> template, ItemLike base, TagKey<Item> addition, ItemLike result) {
        this.smithing(category, Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, TagKey<Item> template, ItemLike base, Ingredient addition, ItemLike result) {
        this.smithing(category, Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, TagKey<Item> template, TagKey<Item> base, ItemLike addition, ItemLike result) {
        this.smithing(category, Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, TagKey<Item> template, TagKey<Item> base, TagKey<Item> addition, ItemLike result) {
        this.smithing(category, Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, TagKey<Item> template, TagKey<Item> base, Ingredient addition, ItemLike result) {
        this.smithing(category, Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, TagKey<Item> template, Ingredient base, ItemLike addition, ItemLike result) {
        this.smithing(category, Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, TagKey<Item> template, Ingredient base, TagKey<Item> addition, ItemLike result) {
        this.smithing(category, Ingredient.of(template), base, addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, TagKey<Item> template, Ingredient base, Ingredient addition, ItemLike result) {
        this.smithing(category, Ingredient.of(template), base, addition, result);
    }
    
    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, Ingredient template, ItemLike base, ItemLike addition, ItemLike result) {
        this.smithing(category, template, Ingredient.of(base), Ingredient.of(addition), result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, Ingredient template, ItemLike base, TagKey<Item> addition, ItemLike result) {
        this.smithing(category, template, Ingredient.of(base), Ingredient.of(addition), result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, Ingredient template, ItemLike base, Ingredient addition, ItemLike result) {
        this.smithing(category, template, Ingredient.of(base), addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, Ingredient template, TagKey<Item> base, ItemLike addition, ItemLike result) {
        this.smithing(category, template, Ingredient.of(base), Ingredient.of(addition), result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, Ingredient template, TagKey<Item> base, TagKey<Item> addition, ItemLike result) {
        this.smithing(category, template, Ingredient.of(base), Ingredient.of(addition), result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, Ingredient template, TagKey<Item> base, Ingredient addition, ItemLike result) {
        this.smithing(category, template, Ingredient.of(base), addition, result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, Ingredient template, Ingredient base, ItemLike addition, ItemLike result) {
        this.smithing(category, template, base, Ingredient.of(addition), result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, Ingredient template, Ingredient base, TagKey<Item> addition, ItemLike result) {
        this.smithing(category, template, base, Ingredient.of(addition), result);
    }

    /**
     * Adds a smithing recipe with the given inputs and output.
     */
    default void smithing(RecipeCategory category, Ingredient template, Ingredient base, Ingredient addition, ItemLike result) {
        SmithingTransformRecipeBuilder builder = SmithingTransformRecipeBuilder.smithing(template, base, addition, category, result.asItem());
        List<CriterionTriggerInstance> criteria = this.criteria(base);
        for (int i = 0; i < criteria.size(); i++) {
            builder.unlocks("has_item" + i, criteria.get(i));
        }
        builder.save(this.consumer(), this.provider().loc(result, "smithing"));
    }
}

package org.moddingx.libx.datagen.provider.recipe;

import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Objects;

/**
 * A {@link RecipeExtension} for stone cutting recipes.
 */
public interface StoneCuttingExtension extends RecipeExtension {
    
    /**
     * Adds a stone cutting recipe with the given input and output.
     */
    default void stoneCutting(ItemLike input, ItemLike output) {
        this.stoneCutting(RecipeCategory.BUILDING_BLOCKS, input, output);
    }

    /**
     * Adds a stone cutting recipe with the given input, output and output amount.
     */
    default void stoneCutting(ItemLike input, ItemLike output, int amount) {
        this.stoneCutting(RecipeCategory.BUILDING_BLOCKS, input, output, amount);
    }

    /**
     * Adds a stone cutting recipe with the given input and output.
     */
    default void stoneCutting(TagKey<Item> input, ItemLike output) {
        this.stoneCutting(RecipeCategory.BUILDING_BLOCKS, input, output);
    }

    /**
     * Adds a stone cutting recipe with the given input, output and output amount.
     */
    default void stoneCutting(TagKey<Item> input, ItemLike output, int amount) {
        this.stoneCutting(RecipeCategory.BUILDING_BLOCKS, input, output, amount);
    }

    /**
     * Adds a stone cutting recipe with the given input and output.
     */
    default void stoneCutting(Ingredient input, ItemLike output) {
        this.stoneCutting(RecipeCategory.BUILDING_BLOCKS, input, output);
    }

    /**
     * Adds a stone cutting recipe with the given input, output and output amount.
     */
    default void stoneCutting(Ingredient input, ItemLike output, int amount) {
        this.stoneCutting(RecipeCategory.BUILDING_BLOCKS, input, output, amount);
    }

    /**
     * Adds a stone cutting recipe with the given input and output.
     */
    default void stoneCutting(Ingredient input, ItemLike output, String suffix) {
        this.stoneCutting(RecipeCategory.BUILDING_BLOCKS, input, output, suffix);
    }

    /**
     * Adds a stone cutting recipe with the given input, output and output amount.
     */
    default void stoneCutting(Ingredient input, ItemLike output, int amount, String suffix) {
        this.stoneCutting(RecipeCategory.BUILDING_BLOCKS, input, output, amount, suffix);
    }
    
    /**
     * Adds a stone cutting recipe with the given input and output.
     */
    default void stoneCutting(RecipeCategory category, ItemLike input, ItemLike output) {
        this.stoneCutting(category, input, output, 1);
    }

    /**
     * Adds a stone cutting recipe with the given input, output and output amount.
     */
    default void stoneCutting(RecipeCategory category, ItemLike input, ItemLike output, int amount) {
        this.stoneCutting(category, Ingredient.of(input), output, amount, "stonecutting_from_" + Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(input.asItem())).getPath());
    }

    /**
     * Adds a stone cutting recipe with the given input and output.
     */
    default void stoneCutting(RecipeCategory category, TagKey<Item> input, ItemLike output) {
        this.stoneCutting(category, input, output, 1);
    }

    /**
     * Adds a stone cutting recipe with the given input, output and output amount.
     */
    default void stoneCutting(RecipeCategory category, TagKey<Item> input, ItemLike output, int amount) {
        this.stoneCutting(category, Ingredient.of(input), output, amount, "stonecutting_from_" + input.location().getPath());
    }

    /**
     * Adds a stone cutting recipe with the given input and output.
     */
    default void stoneCutting(RecipeCategory category, Ingredient input, ItemLike output) {
        this.stoneCutting(category, input, output, "stonecutting");
    }

    /**
     * Adds a stone cutting recipe with the given input, output and output amount.
     */
    default void stoneCutting(RecipeCategory category, Ingredient input, ItemLike output, int amount) {
        this.stoneCutting(category, input, output, amount, "stonecutting");
    }

    /**
     * Adds a stone cutting recipe with the given input and output.
     */
    default void stoneCutting(RecipeCategory category, Ingredient input, ItemLike output, String suffix) {
        this.stoneCutting(category, input, output, 1, suffix);
    }

    /**
     * Adds a stone cutting recipe with the given input, output and output amount.
     */
    default void stoneCutting(RecipeCategory category, Ingredient input, ItemLike output, int amount, String suffix) {
        SingleItemRecipeBuilder builder = SingleItemRecipeBuilder.stonecutting(input, category, output, amount);
        List<CriterionTriggerInstance> criteria = this.criteria(input);
        for (int i = 0; i < criteria.size(); i++) {
            builder.unlockedBy("has_item" + i, criteria.get(i));
        }
        builder.save(this.consumer(), this.provider().loc(output, suffix));
    }
}

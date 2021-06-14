package io.github.noeppi_noeppi.libx.data.provider.recipe;

import net.minecraft.data.CookingRecipeBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

/**
 * A {@link RecipeExtension} for smelting, blast furnace, smoker and campfire recipes.
 */
public interface SmeltingExtension extends RecipeExtension {

    /**
     * Adds a smelting recipe.
     */
    default void smelting(IItemProvider in, IItemProvider out, float exp, int time) {
        this.smelting(this.provider().loc(out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} and {@code exp} should be the values for the normal furnace. They'll be
     * adjusted for the blast furnace automatically.
     */
    default void blasting(IItemProvider in, IItemProvider out, float exp, int time) {
        this.blasting(this.provider().loc(out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} and {@code exp} should be the values for the normal furnace. They'll be
     * adjusted for the smoker automatically.
     */
    default void cooking(IItemProvider in, IItemProvider out, float exp, int time) {
        this.cooking(this.provider().loc(out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} and {@code exp} should be the values for the normal furnace. They'll be
     * adjusted for the smoker and the campfire automatically.
     */
    default void campfire(IItemProvider in, IItemProvider out, float exp, int time) {
        this.campfire(this.provider().loc(out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe.
     */
    default void smelting(ITag<Item> in, IItemProvider out, float exp, int time) {
        this.smelting(this.provider().loc(out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} and {@code exp} should be the values for the normal furnace. They'll be
     * adjusted for the blast furnace automatically.
     */
    default void blasting(ITag<Item> in, IItemProvider out, float exp, int time) {
        this.blasting(this.provider().loc(out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} and {@code exp} should be the values for the normal furnace. They'll be
     * adjusted for the smoker automatically.
     */
    default void cooking(ITag<Item> in, IItemProvider out, float exp, int time) {
        this.cooking(this.provider().loc(out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} and {@code exp} should be the values for the normal furnace. They'll be
     * adjusted for the smoker and the campfire automatically.
     */
    default void campfire(ITag<Item> in, IItemProvider out, float exp, int time) {
        this.campfire(this.provider().loc(out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe.
     */
    default void smelting(ResourceLocation outputId, IItemProvider in, IItemProvider out, float exp, int time) {
        CookingRecipeBuilder.smeltingRecipe(Ingredient.fromItems(in), out, exp, time)
                .addCriterion("has_item", this.criterion(in))
                .build(this.consumer(), new ResourceLocation(outputId.getNamespace(), "smelting/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} and {@code exp} should be the values for the normal furnace. They'll be
     * adjusted for the blast furnace automatically.
     */
    default void blasting(ResourceLocation outputId, IItemProvider in, IItemProvider out, float exp, int time) {
        this.smelting(outputId, in, out, exp, time);
        CookingRecipeBuilder.blastingRecipe(Ingredient.fromItems(in), out, exp / 2, time / 2)
                .addCriterion("has_item", this.criterion(in))
                .build(this.consumer(), new ResourceLocation(outputId.getNamespace(), "blasting/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} and {@code exp} should be the values for the normal furnace. They'll be
     * adjusted for the smoker automatically.
     */
    default void cooking(ResourceLocation outputId, IItemProvider in, IItemProvider out, float exp, int time) {
        this.smelting(outputId, in, out, exp, time);
        CookingRecipeBuilder.cookingRecipe(Ingredient.fromItems(in), out, exp / 2, time / 2, IRecipeSerializer.SMOKING)
                .addCriterion("has_item", this.criterion(in))
                .build(this.consumer(), new ResourceLocation(outputId.getNamespace(), "cooking/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} and {@code exp} should be the values for the normal furnace. They'll be
     * adjusted for the smoker and the campfire automatically.
     */
    default void campfire(ResourceLocation outputId, IItemProvider in, IItemProvider out, float exp, int time) {
        this.cooking(outputId, in, out, exp, time);
        CookingRecipeBuilder.cookingRecipe(Ingredient.fromItems(in), out, exp / 2, time * 3, IRecipeSerializer.CAMPFIRE_COOKING)
                .addCriterion("has_item", this.criterion(in))
                .build(this.consumer(), new ResourceLocation(outputId.getNamespace(), "campfire/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe.
     */
    default void smelting(ResourceLocation outputId, ITag<Item> in, IItemProvider out, float exp, int time) {
        CookingRecipeBuilder.smeltingRecipe(Ingredient.fromTag(in), out, exp, time)
                .addCriterion("has_item", this.criterion(in))
                .build(this.consumer(), new ResourceLocation(outputId.getNamespace(), "smelting/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} and {@code exp} should be the values for the normal furnace. They'll be
     * adjusted for the blast furnace automatically.
     */
    default void blasting(ResourceLocation outputId, ITag<Item> in, IItemProvider out, float exp, int time) {
        this.smelting(outputId, in, out, exp, time);
        CookingRecipeBuilder.blastingRecipe(Ingredient.fromTag(in), out, exp / 2, time / 2)
                .addCriterion("has_item", this.criterion(in))
                .build(this.consumer(), new ResourceLocation(outputId.getNamespace(), "blasting/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} and {@code exp} should be the values for the normal furnace. They'll be
     * adjusted for the smoker automatically.
     */
    default void cooking(ResourceLocation outputId, ITag<Item> in, IItemProvider out, float exp, int time) {
        this.smelting(outputId, in, out, exp, time);
        CookingRecipeBuilder.cookingRecipe(Ingredient.fromTag(in), out, exp / 2, time / 2, IRecipeSerializer.SMOKING)
                .addCriterion("has_item", this.criterion(in))
                .build(this.consumer(), new ResourceLocation(outputId.getNamespace(), "cooking/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} and {@code exp} should be the values for the normal furnace. They'll be
     * adjusted for the smoker and the campfire automatically.
     */
    default void campfire(ResourceLocation outputId, ITag<Item> in, IItemProvider out, float exp, int time) {
        this.cooking(outputId, in, out, exp, time);
        CookingRecipeBuilder.cookingRecipe(Ingredient.fromTag(in), out, exp / 2, time * 3, IRecipeSerializer.CAMPFIRE_COOKING)
                .addCriterion("has_item", this.criterion(in))
                .build(this.consumer(), new ResourceLocation(outputId.getNamespace(), "campfire/" + outputId.getPath()));
    }
}

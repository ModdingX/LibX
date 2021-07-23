package io.github.noeppi_noeppi.libx.data.provider.recipe;

import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.resources.ResourceLocation;

/**
 * A {@link RecipeExtension} for smelting, blast furnace, smoker and campfire recipes.
 */
public interface SmeltingExtension extends RecipeExtension {

    /**
     * Adds a smelting recipe.
     */
    default void smelting(ItemLike in, ItemLike out, float exp, int time) {
        this.smelting(this.provider().loc(out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} and {@code exp} should be the values for the normal furnace. They'll be
     * adjusted for the blast furnace automatically.
     */
    default void blasting(ItemLike in, ItemLike out, float exp, int time) {
        this.blasting(this.provider().loc(out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} and {@code exp} should be the values for the normal furnace. They'll be
     * adjusted for the smoker automatically.
     */
    default void cooking(ItemLike in, ItemLike out, float exp, int time) {
        this.cooking(this.provider().loc(out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} and {@code exp} should be the values for the normal furnace. They'll be
     * adjusted for the smoker and the campfire automatically.
     */
    default void campfire(ItemLike in, ItemLike out, float exp, int time) {
        this.campfire(this.provider().loc(out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe.
     */
    default void smelting(Tag<Item> in, ItemLike out, float exp, int time) {
        this.smelting(this.provider().loc(out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} and {@code exp} should be the values for the normal furnace. They'll be
     * adjusted for the blast furnace automatically.
     */
    default void blasting(Tag<Item> in, ItemLike out, float exp, int time) {
        this.blasting(this.provider().loc(out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} and {@code exp} should be the values for the normal furnace. They'll be
     * adjusted for the smoker automatically.
     */
    default void cooking(Tag<Item> in, ItemLike out, float exp, int time) {
        this.cooking(this.provider().loc(out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} and {@code exp} should be the values for the normal furnace. They'll be
     * adjusted for the smoker and the campfire automatically.
     */
    default void campfire(Tag<Item> in, ItemLike out, float exp, int time) {
        this.campfire(this.provider().loc(out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe.
     */
    default void smelting(ResourceLocation outputId, ItemLike in, ItemLike out, float exp, int time) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(in), out, exp, time)
                .unlockedBy("has_item", this.criterion(in))
                .save(this.consumer(), new ResourceLocation(outputId.getNamespace(), "smelting/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} and {@code exp} should be the values for the normal furnace. They'll be
     * adjusted for the blast furnace automatically.
     */
    default void blasting(ResourceLocation outputId, ItemLike in, ItemLike out, float exp, int time) {
        this.smelting(outputId, in, out, exp, time);
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(in), out, exp / 2, time / 2)
                .unlockedBy("has_item", this.criterion(in))
                .save(this.consumer(), new ResourceLocation(outputId.getNamespace(), "blasting/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} and {@code exp} should be the values for the normal furnace. They'll be
     * adjusted for the smoker automatically.
     */
    default void cooking(ResourceLocation outputId, ItemLike in, ItemLike out, float exp, int time) {
        this.smelting(outputId, in, out, exp, time);
        SimpleCookingRecipeBuilder.cooking(Ingredient.of(in), out, exp / 2, time / 2, RecipeSerializer.SMOKING_RECIPE)
                .unlockedBy("has_item", this.criterion(in))
                .save(this.consumer(), new ResourceLocation(outputId.getNamespace(), "cooking/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} and {@code exp} should be the values for the normal furnace. They'll be
     * adjusted for the smoker and the campfire automatically.
     */
    default void campfire(ResourceLocation outputId, ItemLike in, ItemLike out, float exp, int time) {
        this.cooking(outputId, in, out, exp, time);
        SimpleCookingRecipeBuilder.cooking(Ingredient.of(in), out, exp / 2, time * 3, RecipeSerializer.CAMPFIRE_COOKING_RECIPE)
                .unlockedBy("has_item", this.criterion(in))
                .save(this.consumer(), new ResourceLocation(outputId.getNamespace(), "campfire/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe.
     */
    default void smelting(ResourceLocation outputId, Tag<Item> in, ItemLike out, float exp, int time) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(in), out, exp, time)
                .unlockedBy("has_item", this.criterion(in))
                .save(this.consumer(), new ResourceLocation(outputId.getNamespace(), "smelting/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} and {@code exp} should be the values for the normal furnace. They'll be
     * adjusted for the blast furnace automatically.
     */
    default void blasting(ResourceLocation outputId, Tag<Item> in, ItemLike out, float exp, int time) {
        this.smelting(outputId, in, out, exp, time);
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(in), out, exp / 2, time / 2)
                .unlockedBy("has_item", this.criterion(in))
                .save(this.consumer(), new ResourceLocation(outputId.getNamespace(), "blasting/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} and {@code exp} should be the values for the normal furnace. They'll be
     * adjusted for the smoker automatically.
     */
    default void cooking(ResourceLocation outputId, Tag<Item> in, ItemLike out, float exp, int time) {
        this.smelting(outputId, in, out, exp, time);
        SimpleCookingRecipeBuilder.cooking(Ingredient.of(in), out, exp / 2, time / 2, RecipeSerializer.SMOKING_RECIPE)
                .unlockedBy("has_item", this.criterion(in))
                .save(this.consumer(), new ResourceLocation(outputId.getNamespace(), "cooking/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} and {@code exp} should be the values for the normal furnace. They'll be
     * adjusted for the smoker and the campfire automatically.
     */
    default void campfire(ResourceLocation outputId, Tag<Item> in, ItemLike out, float exp, int time) {
        this.cooking(outputId, in, out, exp, time);
        SimpleCookingRecipeBuilder.cooking(Ingredient.of(in), out, exp / 2, time * 3, RecipeSerializer.CAMPFIRE_COOKING_RECIPE)
                .unlockedBy("has_item", this.criterion(in))
                .save(this.consumer(), new ResourceLocation(outputId.getNamespace(), "campfire/" + outputId.getPath()));
    }
}

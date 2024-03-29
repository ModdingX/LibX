package org.moddingx.libx.datagen.provider.recipe;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/**
 * A {@link RecipeExtension} for smelting, blast furnace, smoker and campfire recipes.
 */
public interface SmeltingExtension extends RecipeExtension {

    /**
     * Adds a smelting recipe.
     */
    default void smelting(ItemLike in, ItemLike out, float exp, int time) {
        this.smelting(RecipeCategory.MISC, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the blast
     * furnace automatically.
     */
    default void blasting(ItemLike in, ItemLike out, float exp, int time) {
        this.blasting(RecipeCategory.MISC, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for
     * the smoker automatically.
     */
    default void cooking(ItemLike in, ItemLike out, float exp, int time) {
        this.cooking(RecipeCategory.MISC, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the smoker
     * and the campfire automatically.
     */
    default void campfire(ItemLike in, ItemLike out, float exp, int time) {
        this.campfire(RecipeCategory.MISC, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe.
     */
    default void smelting(TagKey<Item> in, ItemLike out, float exp, int time) {
        this.smelting(RecipeCategory.MISC, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the blast
     * furnace automatically.
     */
    default void blasting(TagKey<Item> in, ItemLike out, float exp, int time) {
        this.blasting(RecipeCategory.MISC, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} should be the value for the normal furnace. They'll be adjusted for
     * the smoker automatically.
     */
    default void cooking(TagKey<Item> in, ItemLike out, float exp, int time) {
        this.cooking(RecipeCategory.MISC, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the smoker
     * and the campfire automatically.
     */
    default void campfire(TagKey<Item> in, ItemLike out, float exp, int time) {
        this.campfire(RecipeCategory.MISC, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe.
     */
    default void smelting(ResourceLocation outputId, ItemLike in, ItemLike out, float exp, int time) {
        this.smelting(outputId, RecipeCategory.MISC, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the blast
     * furnace automatically.
     */
    default void blasting(ResourceLocation outputId, ItemLike in, ItemLike out, float exp, int time) {
        this.blasting(outputId, RecipeCategory.MISC, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for
     * the smoker automatically.
     */
    default void cooking(ResourceLocation outputId, ItemLike in, ItemLike out, float exp, int time) {
        this.cooking(outputId, RecipeCategory.MISC, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the smoker
     * and the campfire automatically.
     */
    default void campfire(ResourceLocation outputId, ItemLike in, ItemLike out, float exp, int time) {
        this.campfire(outputId, RecipeCategory.MISC, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe.
     */
    default void smelting(ResourceLocation outputId, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.smelting(outputId, RecipeCategory.MISC, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the blast
     * furnace automatically.
     */
    default void blasting(ResourceLocation outputId, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.blasting(outputId, RecipeCategory.MISC, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} should be the value for the normal furnace. They'll be adjusted for
     * the smoker automatically.
     */
    default void cooking(ResourceLocation outputId, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.cooking(outputId, RecipeCategory.MISC, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the smoker
     * and the campfire automatically.
     */
    default void campfire(ResourceLocation outputId, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.campfire(outputId, RecipeCategory.MISC, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe.
     */
    default void smelting(RecipeCategory category, ItemLike in, ItemLike out, float exp, int time) {
        this.smelting(this.provider().loc(out), category, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the blast
     * furnace automatically.
     */
    default void blasting(RecipeCategory category, ItemLike in, ItemLike out, float exp, int time) {
        this.blasting(this.provider().loc(out), category, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for
     * the smoker automatically.
     */
    default void cooking(RecipeCategory category, ItemLike in, ItemLike out, float exp, int time) {
        this.cooking(this.provider().loc(out), category, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the smoker
     * and the campfire automatically.
     */
    default void campfire(RecipeCategory category, ItemLike in, ItemLike out, float exp, int time) {
        this.campfire(this.provider().loc(out), category, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe.
     */
    default void smelting(RecipeCategory category, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.smelting(this.provider().loc(out), category, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the blast
     * furnace automatically.
     */
    default void blasting(RecipeCategory category, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.blasting(this.provider().loc(out), category, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} should be the value for the normal furnace. They'll be adjusted for
     * the smoker automatically.
     */
    default void cooking(RecipeCategory category, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.cooking(this.provider().loc(out), category, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the smoker
     * and the campfire automatically.
     */
    default void campfire(RecipeCategory category, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.campfire(this.provider().loc(out), category, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe.
     */
    default void smelting(ResourceLocation outputId, RecipeCategory category, ItemLike in, ItemLike out, float exp, int time) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(in), category, out, exp, time)
                .unlockedBy("has_item", this.criterion(in))
                .save(this.consumer(), new ResourceLocation(outputId.getNamespace(), "smelting/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the blast
     * furnace automatically.
     */
    default void blasting(ResourceLocation outputId, RecipeCategory category, ItemLike in, ItemLike out, float exp, int time) {
        this.smelting(outputId, in, out, exp, time);
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(in), category, out, exp, time / 2)
                .unlockedBy("has_item", this.criterion(in))
                .save(this.consumer(), new ResourceLocation(outputId.getNamespace(), "blasting/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for
     * the smoker automatically.
     */
    default void cooking(ResourceLocation outputId, RecipeCategory category, ItemLike in, ItemLike out, float exp, int time) {
        this.smelting(outputId, in, out, exp, time);
        SimpleCookingRecipeBuilder.smoking(Ingredient.of(in), category, out, exp, time / 2)
                .unlockedBy("has_item", this.criterion(in))
                .save(this.consumer(), new ResourceLocation(outputId.getNamespace(), "cooking/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the smoker
     * and the campfire automatically.
     */
    default void campfire(ResourceLocation outputId, RecipeCategory category, ItemLike in, ItemLike out, float exp, int time) {
        this.cooking(outputId, in, out, exp, time);
        SimpleCookingRecipeBuilder.campfireCooking(Ingredient.of(in), category, out, exp, time * 3)
                .unlockedBy("has_item", this.criterion(in))
                .save(this.consumer(), new ResourceLocation(outputId.getNamespace(), "campfire/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe.
     */
    default void smelting(ResourceLocation outputId, RecipeCategory category, TagKey<Item> in, ItemLike out, float exp, int time) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(in), category, out, exp, time)
                .unlockedBy("has_item", this.criterion(in))
                .save(this.consumer(), new ResourceLocation(outputId.getNamespace(), "smelting/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the blast
     * furnace automatically.
     */
    default void blasting(ResourceLocation outputId, RecipeCategory category, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.smelting(outputId, in, out, exp, time);
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(in), category, out, exp, time / 2)
                .unlockedBy("has_item", this.criterion(in))
                .save(this.consumer(), new ResourceLocation(outputId.getNamespace(), "blasting/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for
     * the smoker automatically.
     */
    default void cooking(ResourceLocation outputId, RecipeCategory category, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.smelting(outputId, in, out, exp, time);
        SimpleCookingRecipeBuilder.smoking(Ingredient.of(in), category, out, exp, time / 2)
                .unlockedBy("has_item", this.criterion(in))
                .save(this.consumer(), new ResourceLocation(outputId.getNamespace(), "cooking/" + outputId.getPath()));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the smoker
     * and the campfire automatically.
     */
    default void campfire(ResourceLocation outputId, RecipeCategory category, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.cooking(outputId, in, out, exp, time);
        SimpleCookingRecipeBuilder.campfireCooking(Ingredient.of(in), category, out, exp, time * 3)
                .unlockedBy("has_item", this.criterion(in))
                .save(this.consumer(), new ResourceLocation(outputId.getNamespace(), "campfire/" + outputId.getPath()));
    }
}

package org.moddingx.libx.datagen.provider.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/**
 * A {@link RecipeExtension} for smelting, blast furnace, smoker and campfire recipes.
 *
 * <p>The {@link CookingBookCategory} of the generated recipes is derived from the
 * {@link RecipeCategory} that is passed in, not from the result item: a recipe declared with
 * {@link RecipeCategory#FOOD} lands in the food section of the recipe book, everything else in
 * the blocks or misc section, depending on whether the result is a {@link BlockItem}. Food
 * recipes must therefore be declared with {@link RecipeCategory#FOOD} to show up under food.
 * The overloads without a {@link RecipeCategory} default to {@link RecipeCategory#MISC}.
 *
 * <p>As that inference is not always what is wanted, {@code smelting} and {@code blasting} have
 * overloads that take an explicit {@link CookingBookCategory} right after the
 * {@link RecipeCategory}. Those bypass the inference entirely. {@code cooking} and
 * {@code campfire} have no such overloads: smoker and campfire recipes are always
 * {@link CookingBookCategory#FOOD} in vanilla and cannot be given another category.
 */
public interface SmeltingExtension extends RecipeExtension {

    /**
     * Determines the {@link CookingBookCategory} for a smelting result. Furnace recipes need an
     * explicit cooking book category, which is derived from the {@link RecipeCategory} of the
     * recipe. The result item is only inspected to tell blocks from other items, its item
     * components are never queried, as those are not available during datagen.
     */
    private static CookingBookCategory smeltingCategory(RecipeCategory category, ItemLike out) {
        if (category == RecipeCategory.FOOD) {
            return CookingBookCategory.FOOD;
        } else if (out.asItem() instanceof BlockItem) {
            return CookingBookCategory.BLOCKS;
        } else {
            return CookingBookCategory.MISC;
        }
    }

    /**
     * Determines the {@link CookingBookCategory} for a blasting result. A blast furnace cannot cook
     * food, so unlike {@link #smeltingCategory(RecipeCategory, ItemLike)} this never yields
     * {@link CookingBookCategory#FOOD}, no matter the {@link RecipeCategory}.
     */
    private static CookingBookCategory blastingCategory(RecipeCategory category, ItemLike out) {
        return out.asItem() instanceof BlockItem ? CookingBookCategory.BLOCKS : CookingBookCategory.MISC;
    }

    /**
     * Adds the blast furnace recipe alone, without the matching furnace recipe. This exists because
     * the public {@code blasting} methods emit two recipes that may end up in different
     * {@link CookingBookCategory cooking book categories}.
     */
    private void blastingRecipe(Identifier outputId, RecipeCategory category, CookingBookCategory bookCategory, ItemLike in, ItemLike out, float exp, int time) {
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(in), category, bookCategory, out, exp, time / 2)
                .unlockedBy("has_item", this.criterion(in))
                .save(this.output(), ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(outputId.getNamespace(), "blasting/" + outputId.getPath())));
    }

    /**
     * Adds the blast furnace recipe alone, without the matching furnace recipe. This exists because
     * the public {@code blasting} methods emit two recipes that may end up in different
     * {@link CookingBookCategory cooking book categories}.
     */
    private void blastingRecipe(Identifier outputId, RecipeCategory category, CookingBookCategory bookCategory, TagKey<Item> in, ItemLike out, float exp, int time) {
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(this.items().getOrThrow(in)), category, bookCategory, out, exp, time / 2)
                .unlockedBy("has_item", this.criterion(in))
                .save(this.output(), ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(outputId.getNamespace(), "blasting/" + outputId.getPath())));
    }

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
     * Adds a smelting recipe with an explicit {@link CookingBookCategory}.
     */
    default void smelting(CookingBookCategory bookCategory, ItemLike in, ItemLike out, float exp, int time) {
        this.smelting(RecipeCategory.MISC, bookCategory, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe with an explicit {@link CookingBookCategory} that can be performed in a
     * regular furnace and a blast furnace. {@code time} should be the value for the normal furnace.
     * It'll be adjusted for the blast furnace automatically. The {@link CookingBookCategory} is used
     * for both recipes.
     */
    default void blasting(CookingBookCategory bookCategory, ItemLike in, ItemLike out, float exp, int time) {
        this.blasting(RecipeCategory.MISC, bookCategory, in, out, exp, time);
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
     * Adds a smelting recipe with an explicit {@link CookingBookCategory}.
     */
    default void smelting(CookingBookCategory bookCategory, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.smelting(RecipeCategory.MISC, bookCategory, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe with an explicit {@link CookingBookCategory} that can be performed in a
     * regular furnace and a blast furnace. {@code time} should be the value for the normal furnace.
     * It'll be adjusted for the blast furnace automatically. The {@link CookingBookCategory} is used
     * for both recipes.
     */
    default void blasting(CookingBookCategory bookCategory, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.blasting(RecipeCategory.MISC, bookCategory, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe.
     */
    default void smelting(Identifier outputId, ItemLike in, ItemLike out, float exp, int time) {
        this.smelting(outputId, RecipeCategory.MISC, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the blast
     * furnace automatically.
     */
    default void blasting(Identifier outputId, ItemLike in, ItemLike out, float exp, int time) {
        this.blasting(outputId, RecipeCategory.MISC, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for
     * the smoker automatically.
     */
    default void cooking(Identifier outputId, ItemLike in, ItemLike out, float exp, int time) {
        this.cooking(outputId, RecipeCategory.MISC, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the smoker
     * and the campfire automatically.
     */
    default void campfire(Identifier outputId, ItemLike in, ItemLike out, float exp, int time) {
        this.campfire(outputId, RecipeCategory.MISC, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe with an explicit {@link CookingBookCategory}.
     */
    default void smelting(Identifier outputId, CookingBookCategory bookCategory, ItemLike in, ItemLike out, float exp, int time) {
        this.smelting(outputId, RecipeCategory.MISC, bookCategory, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe with an explicit {@link CookingBookCategory} that can be performed in a
     * regular furnace and a blast furnace. {@code time} should be the value for the normal furnace.
     * It'll be adjusted for the blast furnace automatically. The {@link CookingBookCategory} is used
     * for both recipes.
     */
    default void blasting(Identifier outputId, CookingBookCategory bookCategory, ItemLike in, ItemLike out, float exp, int time) {
        this.blasting(outputId, RecipeCategory.MISC, bookCategory, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe.
     */
    default void smelting(Identifier outputId, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.smelting(outputId, RecipeCategory.MISC, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the blast
     * furnace automatically.
     */
    default void blasting(Identifier outputId, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.blasting(outputId, RecipeCategory.MISC, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} should be the value for the normal furnace. They'll be adjusted for
     * the smoker automatically.
     */
    default void cooking(Identifier outputId, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.cooking(outputId, RecipeCategory.MISC, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the smoker
     * and the campfire automatically.
     */
    default void campfire(Identifier outputId, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.campfire(outputId, RecipeCategory.MISC, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe with an explicit {@link CookingBookCategory}.
     */
    default void smelting(Identifier outputId, CookingBookCategory bookCategory, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.smelting(outputId, RecipeCategory.MISC, bookCategory, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe with an explicit {@link CookingBookCategory} that can be performed in a
     * regular furnace and a blast furnace. {@code time} should be the value for the normal furnace.
     * It'll be adjusted for the blast furnace automatically. The {@link CookingBookCategory} is used
     * for both recipes.
     */
    default void blasting(Identifier outputId, CookingBookCategory bookCategory, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.blasting(outputId, RecipeCategory.MISC, bookCategory, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe.
     */
    default void smelting(RecipeCategory category, ItemLike in, ItemLike out, float exp, int time) {
        this.smelting(this.provider().id(out), category, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the blast
     * furnace automatically.
     */
    default void blasting(RecipeCategory category, ItemLike in, ItemLike out, float exp, int time) {
        this.blasting(this.provider().id(out), category, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for
     * the smoker automatically.
     */
    default void cooking(RecipeCategory category, ItemLike in, ItemLike out, float exp, int time) {
        this.cooking(this.provider().id(out), category, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the smoker
     * and the campfire automatically.
     */
    default void campfire(RecipeCategory category, ItemLike in, ItemLike out, float exp, int time) {
        this.campfire(this.provider().id(out), category, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe with an explicit {@link CookingBookCategory}.
     */
    default void smelting(RecipeCategory category, CookingBookCategory bookCategory, ItemLike in, ItemLike out, float exp, int time) {
        this.smelting(this.provider().id(out), category, bookCategory, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe with an explicit {@link CookingBookCategory} that can be performed in a
     * regular furnace and a blast furnace. {@code time} should be the value for the normal furnace.
     * It'll be adjusted for the blast furnace automatically. The {@link CookingBookCategory} is used
     * for both recipes.
     */
    default void blasting(RecipeCategory category, CookingBookCategory bookCategory, ItemLike in, ItemLike out, float exp, int time) {
        this.blasting(this.provider().id(out), category, bookCategory, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe.
     */
    default void smelting(RecipeCategory category, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.smelting(this.provider().id(out), category, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the blast
     * furnace automatically.
     */
    default void blasting(RecipeCategory category, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.blasting(this.provider().id(out), category, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} should be the value for the normal furnace. They'll be adjusted for
     * the smoker automatically.
     */
    default void cooking(RecipeCategory category, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.cooking(this.provider().id(out), category, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the smoker
     * and the campfire automatically.
     */
    default void campfire(RecipeCategory category, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.campfire(this.provider().id(out), category, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe with an explicit {@link CookingBookCategory}.
     */
    default void smelting(RecipeCategory category, CookingBookCategory bookCategory, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.smelting(this.provider().id(out), category, bookCategory, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe with an explicit {@link CookingBookCategory} that can be performed in a
     * regular furnace and a blast furnace. {@code time} should be the value for the normal furnace.
     * It'll be adjusted for the blast furnace automatically. The {@link CookingBookCategory} is used
     * for both recipes.
     */
    default void blasting(RecipeCategory category, CookingBookCategory bookCategory, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.blasting(this.provider().id(out), category, bookCategory, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe.
     */
    default void smelting(Identifier outputId, RecipeCategory category, ItemLike in, ItemLike out, float exp, int time) {
        this.smelting(outputId, category, smeltingCategory(category, out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the blast
     * furnace automatically.
     */
    default void blasting(Identifier outputId, RecipeCategory category, ItemLike in, ItemLike out, float exp, int time) {
        // The furnace recipe and the blast furnace recipe may end up in different cooking book
        // categories, as a blast furnace cannot cook food, so they are inferred separately.
        this.smelting(outputId, category, in, out, exp, time);
        this.blastingRecipe(outputId, category, blastingCategory(category, out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe with an explicit {@link CookingBookCategory}.
     */
    default void smelting(Identifier outputId, RecipeCategory category, CookingBookCategory bookCategory, ItemLike in, ItemLike out, float exp, int time) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(in), category, bookCategory, out, exp, time)
                .unlockedBy("has_item", this.criterion(in))
                .save(this.output(), ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(outputId.getNamespace(), "smelting/" + outputId.getPath())));
    }

    /**
     * Adds a smelting recipe with an explicit {@link CookingBookCategory} that can be performed in a
     * regular furnace and a blast furnace. {@code time} should be the value for the normal furnace.
     * It'll be adjusted for the blast furnace automatically. The {@link CookingBookCategory} is used
     * for both recipes.
     */
    default void blasting(Identifier outputId, RecipeCategory category, CookingBookCategory bookCategory, ItemLike in, ItemLike out, float exp, int time) {
        this.smelting(outputId, category, bookCategory, in, out, exp, time);
        this.blastingRecipe(outputId, category, bookCategory, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for
     * the smoker automatically.
     */
    default void cooking(Identifier outputId, RecipeCategory category, ItemLike in, ItemLike out, float exp, int time) {
        this.smelting(outputId, category, in, out, exp, time);
        SimpleCookingRecipeBuilder.smoking(Ingredient.of(in), category, out, exp, time / 2)
                .unlockedBy("has_item", this.criterion(in))
                .save(this.output(), ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(outputId.getNamespace(), "cooking/" + outputId.getPath())));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the smoker
     * and the campfire automatically.
     */
    default void campfire(Identifier outputId, RecipeCategory category, ItemLike in, ItemLike out, float exp, int time) {
        this.cooking(outputId, category, in, out, exp, time);
        SimpleCookingRecipeBuilder.campfireCooking(Ingredient.of(in), category, out, exp, time * 3)
                .unlockedBy("has_item", this.criterion(in))
                .save(this.output(), ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(outputId.getNamespace(), "campfire/" + outputId.getPath())));
    }

    /**
     * Adds a smelting recipe.
     */
    default void smelting(Identifier outputId, RecipeCategory category, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.smelting(outputId, category, smeltingCategory(category, out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a blast furnace.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the blast
     * furnace automatically.
     */
    default void blasting(Identifier outputId, RecipeCategory category, TagKey<Item> in, ItemLike out, float exp, int time) {
        // The furnace recipe and the blast furnace recipe may end up in different cooking book
        // categories, as a blast furnace cannot cook food, so they are inferred separately.
        this.smelting(outputId, category, in, out, exp, time);
        this.blastingRecipe(outputId, category, blastingCategory(category, out), in, out, exp, time);
    }

    /**
     * Adds a smelting recipe with an explicit {@link CookingBookCategory}.
     */
    default void smelting(Identifier outputId, RecipeCategory category, CookingBookCategory bookCategory, TagKey<Item> in, ItemLike out, float exp, int time) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(this.items().getOrThrow(in)), category, bookCategory, out, exp, time)
                .unlockedBy("has_item", this.criterion(in))
                .save(this.output(), ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(outputId.getNamespace(), "smelting/" + outputId.getPath())));
    }

    /**
     * Adds a smelting recipe with an explicit {@link CookingBookCategory} that can be performed in a
     * regular furnace and a blast furnace. {@code time} should be the value for the normal furnace.
     * It'll be adjusted for the blast furnace automatically. The {@link CookingBookCategory} is used
     * for both recipes.
     */
    default void blasting(Identifier outputId, RecipeCategory category, CookingBookCategory bookCategory, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.smelting(outputId, category, bookCategory, in, out, exp, time);
        this.blastingRecipe(outputId, category, bookCategory, in, out, exp, time);
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace and a smoker.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for
     * the smoker automatically.
     */
    default void cooking(Identifier outputId, RecipeCategory category, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.smelting(outputId, category, in, out, exp, time);
        SimpleCookingRecipeBuilder.smoking(Ingredient.of(this.items().getOrThrow(in)), category, out, exp, time / 2)
                .unlockedBy("has_item", this.criterion(in))
                .save(this.output(), ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(outputId.getNamespace(), "cooking/" + outputId.getPath())));
    }

    /**
     * Adds a smelting recipe that can be performed in a regular furnace, a smoker anda campfire.
     * {@code time} should be the value for the normal furnace. It'll be adjusted for the smoker
     * and the campfire automatically.
     */
    default void campfire(Identifier outputId, RecipeCategory category, TagKey<Item> in, ItemLike out, float exp, int time) {
        this.cooking(outputId, category, in, out, exp, time);
        SimpleCookingRecipeBuilder.campfireCooking(Ingredient.of(this.items().getOrThrow(in)), category, out, exp, time * 3)
                .unlockedBy("has_item", this.criterion(in))
                .save(this.output(), ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(outputId.getNamespace(), "campfire/" + outputId.getPath())));
    }
}

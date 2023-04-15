package org.moddingx.libx.datagen.provider.recipe.crafting;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.level.ItemLike;
import org.moddingx.libx.datagen.provider.recipe.RecipeExtension;

/**
 * A {@link RecipeExtension} with default methods for compressing things.
 */
public interface CompressionExtension extends RecipeExtension {

    default void compress(ItemLike item, ItemLike compressed) {
        this.compress(RecipeCategory.MISC, item, compressed);
    }

    default void compress(ItemLike item, ItemLike compressed, boolean canRevert) {
        this.compress(RecipeCategory.MISC, item, compressed, canRevert);
    }

    default void compress(RecipeCategory recipeCategory, ItemLike item, ItemLike compressed) {
        this.compress(recipeCategory, item, compressed, true);
    }
    
    default void compress(RecipeCategory recipeCategory, ItemLike item, ItemLike compressed, boolean canRevert) {
        ShapedRecipeBuilder.shaped(recipeCategory, compressed)
                .define('a', item)
                .pattern("aaa")
                .pattern("aaa")
                .pattern("aaa")
                .unlockedBy("has_item", this.criterion(item))
                .save(this.consumer(), this.provider().loc(item, "compress"));

        if (canRevert) {
            ShapelessRecipeBuilder.shapeless(recipeCategory, item, 9)
                    .requires(compressed)
                    .unlockedBy("has_item", this.criterion(compressed))
                    .save(this.consumer(), this.provider().loc(compressed, "decompress"));
        }
    }

    default void smallCompress(ItemLike item, ItemLike compressed) {
        this.smallCompress(RecipeCategory.MISC, item, compressed);
    }

    default void smallCompress(ItemLike item, ItemLike compressed, boolean canRevert) {
        this.smallCompress(RecipeCategory.MISC, item, compressed, canRevert);
    }

    default void smallCompress(RecipeCategory recipeCategory, ItemLike item, ItemLike compressed) {
        this.smallCompress(recipeCategory, item, compressed, true);
    }
    
    default void smallCompress(RecipeCategory recipeCategory, ItemLike item, ItemLike compressed, boolean canRevert) {
        ShapedRecipeBuilder.shaped(recipeCategory, compressed)
                .define('a', item)
                .pattern("aa")
                .pattern("aa")
                .unlockedBy("has_item", this.criterion(item))
                .save(this.consumer(), this.provider().loc(item, "small_compress"));

        if (canRevert) {
            ShapelessRecipeBuilder.shapeless(recipeCategory, item, 4)
                    .requires(compressed)
                    .unlockedBy("has_item", this.criterion(compressed))
                    .save(this.consumer(), this.provider().loc(compressed, "small_decompress"));
        }
    }

    default void doubleCompress(ItemLike item, ItemLike compressed, ItemLike doubleCompressed) {
        this.doubleCompress(item, compressed, doubleCompressed, true);
    }

    default void doubleCompress(ItemLike item, ItemLike compressed, ItemLike doubleCompressed, boolean canRevert) {
        this.compress(item, compressed, canRevert);
        this.compress(compressed, doubleCompressed, canRevert);
    }
}

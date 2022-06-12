package org.moddingx.libx.data.provider.recipe.crafting;

import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.level.ItemLike;
import org.moddingx.libx.data.provider.recipe.RecipeExtension;

/**
 * A {@link RecipeExtension} with default methods for compressing things.
 */
public interface CompressionExtension extends RecipeExtension {

    default void compress(ItemLike item, ItemLike compressed) {
        this.compress(item, compressed, true);
    }
    
    default void compress(ItemLike item, ItemLike compressed, boolean canRevert) {
        ShapedRecipeBuilder.shaped(compressed)
                .define('a', item)
                .pattern("aaa")
                .pattern("aaa")
                .pattern("aaa")
                .unlockedBy("has_item", this.criterion(item))
                .save(this.consumer(), this.provider().loc(item, "compress"));

        if (canRevert) {
            ShapelessRecipeBuilder.shapeless(item, 9)
                    .requires(compressed)
                    .unlockedBy("has_item", this.criterion(compressed))
                    .save(this.consumer(), this.provider().loc(compressed, "decompress"));
        }
    }

    default void smallCompress(ItemLike item, ItemLike compressed) {
        this.smallCompress(item, compressed, true);
    }
    
    default void smallCompress(ItemLike item, ItemLike compressed, boolean canRevert) {
        ShapedRecipeBuilder.shaped(compressed)
                .define('a', item)
                .pattern("aa")
                .pattern("aa")
                .unlockedBy("has_item", this.criterion(item))
                .save(this.consumer(), this.provider().loc(item, "small_compress"));

        if (canRevert) {
            ShapelessRecipeBuilder.shapeless(item, 4)
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

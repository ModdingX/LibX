package io.github.noeppi_noeppi.libx.data.provider.recipe.crafting;

import io.github.noeppi_noeppi.libx.data.provider.recipe.RecipeExtension;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.util.IItemProvider;

/**
 * A {@link RecipeExtension} with default methods for compressing things.
 */
public interface CompressionExtension extends RecipeExtension {

    default void compress(IItemProvider item, IItemProvider compressed) {
        this.compress(item, compressed, true);
    }
    
    default void compress(IItemProvider item, IItemProvider compressed, boolean canRevert) {
        ShapedRecipeBuilder.shapedRecipe(compressed)
                .key('a', item)
                .patternLine("aaa")
                .patternLine("aaa")
                .patternLine("aaa")
                .addCriterion("has_item", this.criterion(item))
                .build(this.consumer(), this.provider().loc(item, "compress"));

        if (canRevert) {
            ShapelessRecipeBuilder.shapelessRecipe(item, 9)
                    .addIngredient(compressed)
                    .addCriterion("has_item", this.criterion(compressed))
                    .build(this.consumer(), this.provider().loc(compressed, "decompress"));
        }
    }

    default void smallCompress(IItemProvider item, IItemProvider compressed) {
        this.smallCompress(item, compressed, true);
    }
    
    default void smallCompress(IItemProvider item, IItemProvider compressed, boolean canRevert) {
        ShapedRecipeBuilder.shapedRecipe(compressed)
                .key('a', item)
                .patternLine("aa")
                .patternLine("aa")
                .addCriterion("has_item", this.criterion(item))
                .build(this.consumer(), this.provider().loc(item, "small_compress"));

        if (canRevert) {
            ShapelessRecipeBuilder.shapelessRecipe(item, 4)
                    .addIngredient(compressed)
                    .addCriterion("has_item", this.criterion(compressed))
                    .build(this.consumer(), this.provider().loc(compressed, "small_decompress"));
        }
    }

    default void doubleCompress(IItemProvider item, IItemProvider compressed, IItemProvider doubleCompressed) {
        this.doubleCompress(item, compressed, doubleCompressed, true);
    }

    default void doubleCompress(IItemProvider item, IItemProvider compressed, IItemProvider doubleCompressed, boolean canRevert) {
        this.compress(item, compressed, canRevert);
        this.compress(compressed, doubleCompressed, canRevert);
    }
}

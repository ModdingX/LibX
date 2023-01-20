package org.moddingx.libx.datagen.provider.recipe.crafting;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.moddingx.libx.datagen.provider.recipe.RecipeExtension;
import org.moddingx.libx.impl.datagen.recipe.ObjectCraftingBuilder;

/**
 * A {@link RecipeExtension} for shaped and shapeless recipes.
 */
public interface CraftingExtension extends RecipeExtension {

    /**
     * Adds a new shaped recipe based on the input objects. The input objects must
     * be built like this:
     * 
     * (A sub list means that <b>one</b> of its elements can be used.
     * 
     * <ul>
     *     <li>Optional: A {@link ResourceLocation} that serves as the recipe id.</li>
     *     <li>Optional: A {@link RecipeCategory}. Defaults to {@link RecipeCategory#MISC}</li>
     *     <ul>
     *         <li>An {@link ItemLike} for the output optionally followed by an {@link Integer} for the amount.</li>
     *         <li>An {@link ItemStack} that is used to determine the output item and count.</li>
     *     </ul>
     *     <li>A set of strings which are the pattern lines for the recipe.</li>
     *     <li>The rest of the input must be a {@link Character} followed by an ingredient identifier repeated for each key from the pattern lines.</li>
     * </ul>
     * 
     * An ingredient identifier is one of the following:
     * 
     * <ul>
     *     <li>An {@link ItemLike}</li>
     *     <li>An {@link TagKey TagKey&lt;Item&gt;}</li>
     *     <li>An {@link Ingredient}</li>
     *     <li>A list of the ones above.</li>
     * </ul>
     */
    default void shaped(Object... objects) {
        ObjectCraftingBuilder.buildShaped(this, objects);
    }

    /**
     * Adds a new shapeless recipe based on the input objects. The input objects must
     * be built like this:
     *
     * (A sub list means that <b>one</b> of its elements can be used.
     *
     * <ul>
     *     <li>Optional: A {@link ResourceLocation} that serves as the recipe id.</li>
     *     <ul>
     *         <li>An {@link ItemLike} for the output optionally followed by an {@link Integer} for the amount.</li>
     *         <li>An {@link ItemStack} that is used to determine the output item and count.</li>
     *     </ul>
     *     <li>The rest of the input must be ingredient identifiers which set the required items.</li>
     * </ul>
     *
     * An ingredient identifier is one of the following:
     *
     * <ul>
     *     <li>An {@link ItemLike}</li>
     *     <li>An {@link TagKey TagKey&lt;Item&gt;}</li>
     *     <li>An {@link Ingredient}</li>
     *     <li>A list of the ones above.</li>
     * </ul>
     */
    default void shapeless(Object... objects) {
        ObjectCraftingBuilder.buildShapeless(this, objects);
    }
}

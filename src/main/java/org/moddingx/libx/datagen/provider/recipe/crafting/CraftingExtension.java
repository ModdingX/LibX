package org.moddingx.libx.datagen.provider.recipe.crafting;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.moddingx.libx.datagen.provider.recipe.RecipeExtension;
import org.moddingx.libx.impl.datagen.recipe.ObjectCraftingBuilder;

/**
 * A {@link RecipeExtension} for shaped and shapeless recipes.
 */
public interface CraftingExtension extends RecipeExtension {

    /**
     * Adds a new shaped recipe based on the input objects. The input objects must
     * be built like this:
     *<p>
     * (A sub list means that <b>one</b> of its elements can be used.)
     *
     * <ul>
     *     <li>Optional: A {@link Identifier} that serves as the recipe id.</li>
     *     <li>Optional: A {@link RecipeCategory}. Defaults to {@link RecipeCategory#MISC}</li>
     *     <li>Optional: A single {@link ICondition} or an array of {@link ICondition IConditions} as conditions for the recipe.</li>
     *     <ul>
     *         <li>An {@link ItemLike} for the output, optionally followed by an {@link Integer} for the amount.</li>
     *         <li>An {@link ItemStackTemplate} which is the result of the crafting recipe.</li>
     *     </ul>
     *     <li>One or more {@code String}s that form the pattern lines of the recipe.</li>
     *     <li>For every non-space character used in the pattern: the {@link Character} itself followed by the ingredient identifier that defines that key.</li>
     * </ul>
     *
     * An ingredient identifier is one of the following:
     *
     * <ul>
     *     <li>An {@link ItemLike}</li>
     *     <li>A {@link TagKey TagKey&lt;Item&gt;}</li>
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
     * <p>
     * (A sub list means that <b>one</b> of its elements can be used.)
     *
     * <ul>
     *     <li>Optional: A {@link Identifier} that serves as the recipe id.</li>
     *     <li>Optional: A {@link RecipeCategory}. Defaults to {@link RecipeCategory#MISC}</li>
     *     <li>Optional: A single {@link ICondition} or an array of {@link ICondition IConditions} as conditions for the recipe.</li>
     *     <ul>
     *         <li>An {@link ItemLike} for the output, optionally followed by an {@link Integer} for the amount.</li>
     *         <li>An {@link ItemStackTemplate} which is the result of the crafting recipe.</li>
     *     </ul>
     *     <li>One or more ingredient identifiers that make up the recipe inputs.</li>
     *     <li>Optional: One or more {@code ICondition}s as conditions for the recipe.</li>
     * </ul>
     *
     * An ingredient identifier is one of the following:
     *
     * <ul>
     *     <li>An {@link ItemLike}</li>
     *     <li>A {@link TagKey TagKey&lt;Item&gt;}</li>
     *     <li>An {@link Ingredient}</li>
     *     <li>A list of the ones above.</li>
     * </ul>
     */
    default void shapeless(Object... objects) {
        ObjectCraftingBuilder.buildShapeless(this, objects);
    }
}

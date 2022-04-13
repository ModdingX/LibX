package io.github.noeppi_noeppi.libx.data.provider.recipe;

import io.github.noeppi_noeppi.libx.crafting.ingredient.MergedIngredient;
import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.CompoundIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A recipe extension is an interface that provides logic for a {@link RecipeProviderBase}. Custom recipe
 * extension should extend this interface and then add default methods to be used in {@link RecipeProviderBase#setup()}.
 * As {@link RecipeProviderBase} implements this interface as well, the abstract methods are then filled with logic.
 * 
 * Additionally, a recipe extension class can define a {@code public} {@code static} method named {@code setup} that
 * takes a {@link ModX} and an extension with the same type as the class that defines the method. When
 * a {@link RecipeProviderBase} implements that extension, it'll call that setup method during setup.
 */
public interface RecipeExtension {

    /**
     * Gets the {@link RecipeProviderBase} for this extension.
     */
    RecipeProviderBase provider();
    
    /**
     * Gets the {@link Consumer} for {@link FinishedRecipe finished recipes} to add recipes to.
     */
    Consumer<FinishedRecipe> consumer();

    /**
     * Builds an {@link CriterionTriggerInstance advancement criterion} for the given {@link ItemLike item}.
     */
    CriterionTriggerInstance criterion(ItemLike item);
    
    /**
     * Builds an {@link CriterionTriggerInstance advancement criterion} for the given {@link Tag tag}.
     */
    CriterionTriggerInstance criterion(TagKey<Item> item);
    
    /**
     * Builds an {@link CriterionTriggerInstance advancement criterion} that requires all of the given
     * {@link ItemPredicate items}.
     */
    CriterionTriggerInstance criterion(ItemPredicate... items);

    /**
     * Gets a list of criteria that should be ORed, meaning that the recipe should unlock when one of
     * them is completed instead of all of them.
     */
    default List<CriterionTriggerInstance> criteria(Ingredient item) {
        List<CriterionTriggerInstance> instances = new ArrayList<>();
        if (item.isVanilla()) {
            for (Ingredient.Value entry : item.values) {
                if (entry instanceof Ingredient.ItemValue value) {
                    instances.add(this.criterion(ItemPredicate.Builder.item().of(value.item.getItem()).build()));
                } else if (entry instanceof Ingredient.TagValue value) {
                    instances.add(this.criterion(ItemPredicate.Builder.item().of(value.tag).build()));
                }
            }
        } else if (item instanceof CompoundIngredient cmp) {
            for (Ingredient i : cmp.getChildren()) {
                instances.addAll(this.criteria(i));
            }
        } else if (item instanceof MergedIngredient merged) {
            for (Ingredient i : merged.getIngredients()) {
                instances.addAll(this.criteria(i));
            }
        } else {
            for (ItemStack stack : item.getItems()) {
                instances.add(this.criterion(ItemPredicate.Builder.item().of(stack.getItem()).build()));
            }
        }
        return instances;
    }
}

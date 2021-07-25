package io.github.noeppi_noeppi.libx.data.provider.recipe;

import io.github.noeppi_noeppi.libx.crafting.ingredient.MergedIngredient;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
    CriterionTriggerInstance criterion(Tag<Item> item);
    
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

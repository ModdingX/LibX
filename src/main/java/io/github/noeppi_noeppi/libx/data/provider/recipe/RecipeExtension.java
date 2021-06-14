package io.github.noeppi_noeppi.libx.data.provider.recipe;

import io.github.noeppi_noeppi.libx.crafting.ingredient.MergedIngredient;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public interface RecipeExtension {

    /**
     * Gets the {@link RecipeProviderBase} for this extension.
     */
    RecipeProviderBase provider();
    
    /**
     * Gets the {@link Consumer} for {@link IFinishedRecipe finished recipes} to add recipes to.
     */
    Consumer<IFinishedRecipe> consumer();

    /**
     * Builds an {@link CriterionInstance advancement criterion} for the given {@link IItemProvider item}.
     */
    CriterionInstance criterion(IItemProvider item);
    
    /**
     * Builds an {@link CriterionInstance advancement criterion} for the given {@link ITag tag}.
     */
    CriterionInstance criterion(ITag<Item> item);
    
    /**
     * Builds an {@link CriterionInstance advancement criterion} that requires all of the given
     * {@link ItemPredicate items}.
     */
    CriterionInstance criterion(ItemPredicate... items);

    /**
     * Gets a list of criteria that should be ORed, meaning that the recipe should unlock when one of
     * them is completed instead of all of them.
     */
    default List<CriterionInstance> criteria(Ingredient item) {
        List<CriterionInstance> instances = new ArrayList<>();
        if (item.isVanilla()) {
            for (Ingredient.IItemList entry : item.acceptedItems) {
                if (entry instanceof Ingredient.SingleItemList) {
                    instances.add(this.criterion(ItemPredicate.Builder.create().item(((Ingredient.SingleItemList) entry).stack.getItem()).build()));
                } else if (entry instanceof Ingredient.TagList) {
                    instances.add(this.criterion(ItemPredicate.Builder.create().tag(((Ingredient.TagList) entry).tag).build()));
                }
            }
        } else if (item instanceof MergedIngredient) {
            for (Ingredient i : ((MergedIngredient) item).getIngredients()) {
                instances.addAll(this.criteria(i));
            }
        } else {
            for (ItemStack stack : item.getMatchingStacks()) {
                instances.add(this.criterion(ItemPredicate.Builder.create().item(stack.getItem()).build()));
            }
        }
        return instances;
    }
}

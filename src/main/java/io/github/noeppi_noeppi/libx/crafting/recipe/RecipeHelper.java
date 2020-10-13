package io.github.noeppi_noeppi.libx.crafting.recipe;

import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RecipeHelper {

    /**
     * Gets whether an ItemStack is a valid input for at least one recipe of a given recipe type.
     *
     * @param rm The recipe manager to use. You can get one from a world.
     */
    public static boolean isItemValidInput(RecipeManager rm, IRecipeType<?> recipeType, ItemStack stack) {
        Collection<? extends IRecipe<?>> recipes = rm.getRecipes(recipeType).values();
        for (IRecipe<?> recipe : recipes) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                if (ingredient.test(stack)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks whether all the ingredients of a recipe are present in a list of ItemStacks. This
     * does not check that all ingredients are on different slots.
     *
     * @param exactMatch When this is true this will return false if the stack list contains
     *                   more items than the recipe requires.
     */
    public static boolean matches(IRecipe<?> recipe, List<ItemStack> stacks, boolean exactMatch) {

        ArrayList<Integer> countsLeft = new ArrayList<>();
        for (ItemStack stack : stacks) {
            countsLeft.add(stack.isEmpty() ? 0 : stack.getCount());
        }

        ingredientLoop: for (Ingredient ingredient : recipe.getIngredients()) {
            for (int i = 0; i < stacks.size(); i++) {
                if (countsLeft.get(i) > 0) {
                    if (ingredient.test(stacks.get(i))) {
                        countsLeft.set(i, countsLeft.get(i) - 1);
                        continue ingredientLoop;
                    }
                }
            }
            return false;
        }

        return !exactMatch || countsLeft.stream().noneMatch(count -> count > 0);
    }

    /**
     * Takes a list of ItemStacks and stacks them up so multiple ItemStacks that can be
     * stacked are transformed into one.
     *
     * @param ignoreMaxStackSize Whether this should create ItemStacks with a stack size
     *                           greater than the maximum.
     */
    public List<ItemStack> stackUp(List<ItemStack> stacks, boolean ignoreMaxStackSize) {
        List<ItemStack> stacked = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                int itemsLeft = stack.getCount();
                for (ItemStack used : stacked) {
                    if (Container.areItemsAndTagsEqual(stack, used)) {
                        int stackTransfer = ignoreMaxStackSize ? itemsLeft : Math.min(itemsLeft, used.getMaxStackSize() - used.getCount());
                        if (stackTransfer < 0) {
                            stackTransfer = 0;
                        }
                        used.grow(stackTransfer);
                        itemsLeft -= stackTransfer;
                    }
                }
                if (itemsLeft > 0) {
                    ItemStack newStack = stack.copy();
                    newStack.setCount(itemsLeft);
                    stacked.add(newStack);
                }
            }
        }
        return Collections.unmodifiableList(stacked);
    }
}

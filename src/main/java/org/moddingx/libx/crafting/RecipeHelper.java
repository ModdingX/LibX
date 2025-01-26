package org.moddingx.libx.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RecipeHelper {

    /**
     * Gets whether an {@link ItemStack} is a valid input for at least one recipe of a given recipe type.
     *
     * @param rm The recipe manager to use. You can get one from a world.
     */
    public static <I extends RecipeInput, T extends Recipe<I>> boolean isItemValidInput(RecipeManager rm, RecipeType<T> recipeType, ItemStack stack) {
        Collection<? extends RecipeHolder<T>> recipes = rm.getAllRecipesFor(recipeType);
        for (RecipeHolder<?> recipe : recipes) {
            for (Ingredient ingredient : recipe.value().getIngredients()) {
                if (ingredient.test(stack)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks whether all the ingredients of a recipe are present in a list of {@link ItemStack}s.
     * This does not check that all ingredients are on different slots.
     *
     * @param exactMatch When this is true this will return false if the stack list contains
     *                   more items than the recipe requires.
     */
    public static boolean matches(Recipe<?> recipe, List<ItemStack> stacks, boolean exactMatch) {
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
     * Takes a list of {@link ItemStack ItemStacks} and stacks them up so multiple ItemStacks that can be
     * stacked are transformed into one.
     */
    public static List<ItemStack> stackUp(List<ItemStack> stacks) {
        List<ItemStack> stacked = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                int itemsLeft = stack.getCount();
                for (ItemStack used : stacked) {
                    if (ItemStack.isSameItemSameComponents(stack, used)) {
                        int stackTransfer = Math.min(itemsLeft, used.getMaxStackSize() - used.getCount());
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

package org.moddingx.libx.crafting;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

public class RecipeHelper {

    /**
     * Gets whether an {@link ItemStack} is a valid input for at least one recipe of a given recipe type.
     *
     * @param rm The recipe manager to use. You can get one from a world.
     */
    public static boolean isItemValidInput(RecipeManager rm, RecipeType<?> recipeType, ItemStack stack) {
        //noinspection unchecked
        Collection<? extends Recipe<?>> recipes = rm.byType((RecipeType<Recipe<Container>>) recipeType).values();
        for (Recipe<?> recipe : recipes) {
            for (Ingredient ingredient : recipe.getIngredients()) {
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
                    if (ItemStack.isSameItemSameTags(stack, used)) {
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

    /**
     * Serialises the given {@link ItemStack} to json, so it can be read back by {@link CraftingHelper#getItemStack(JsonObject, boolean)}.
     */
    public static JsonObject serializeItemStack(ItemStack stack, boolean writeNBT) {
        JsonObject json = new JsonObject();
        json.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(stack.getItem()), "Can't serialize ItemStack: Item has no registry name: " + stack.getItem()).toString());
        json.addProperty("count", stack.getCount());
        if (writeNBT) {
            CompoundTag stackTag = stack.hasTag() ? stack.getTag() : null;
            Tag capsTag = forgeCaps(stack);
            if (stackTag != null || capsTag != null) {
                // Need to make a copy, so the stack is not modified.
                CompoundTag resultTag = stackTag == null ? new CompoundTag() : stackTag.copy();
                if (capsTag != null) resultTag.put("ForgeCaps", capsTag);
                json.addProperty("nbt", resultTag.toString());
            }
        }
        return json;
    }

    @Nullable
    private static Tag forgeCaps(ItemStack stack) {
        CompoundTag nbt = stack.serializeNBT();
        if (nbt.contains("ForgeCaps") && (!(nbt.get("ForgeCaps") instanceof CompoundTag cmp) || !cmp.isEmpty())) {
            return nbt.get("ForgeCaps");
        } else {
            return null;
        }
    }
}

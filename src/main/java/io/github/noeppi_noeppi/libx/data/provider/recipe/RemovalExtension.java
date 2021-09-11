package io.github.noeppi_noeppi.libx.data.provider.recipe;

import io.github.noeppi_noeppi.libx.impl.crafting.recipe.EmptyRecipe;
import net.minecraft.resources.ResourceLocation;

public interface RemovalExtension extends RecipeExtension {

    default void remove(ResourceLocation recipe) {
        this.consumer().accept(EmptyRecipe.empty(recipe));
    }
}

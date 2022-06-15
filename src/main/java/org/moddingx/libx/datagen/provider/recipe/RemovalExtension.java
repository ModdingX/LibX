package org.moddingx.libx.datagen.provider.recipe;

import net.minecraft.resources.ResourceLocation;
import org.moddingx.libx.impl.crafting.recipe.EmptyRecipe;

public interface RemovalExtension extends RecipeExtension {

    default void remove(ResourceLocation recipe) {
        this.consumer().accept(EmptyRecipe.empty(recipe));
    }
}

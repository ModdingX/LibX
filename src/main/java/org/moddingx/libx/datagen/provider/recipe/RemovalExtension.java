package org.moddingx.libx.datagen.provider.recipe;

import net.minecraft.resources.ResourceLocation;
import org.moddingx.libx.impl.crafting.recipe.EmptyRecipe;

public interface RemovalExtension extends RecipeExtension {

    default void remove(ResourceLocation recipe) {
        this.output().accept(recipe, EmptyRecipe.empty(), null);
    }
}

package org.moddingx.libx.datagen.provider.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.moddingx.libx.impl.crafting.recipe.EmptyRecipe;

public interface RemovalExtension extends RecipeExtension {

    default void remove(Identifier recipe) {
        this.output().accept(ResourceKey.create(Registries.RECIPE, recipe), EmptyRecipe.empty(), null);
    }
}

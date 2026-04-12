package org.moddingx.libx.datagen.provider.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.moddingx.libx.impl.crafting.recipe.EmptyRecipe;

public interface RemovalExtension extends RecipeExtension {

    default void remove(ResourceLocation recipe) {
        this.output().accept(ResourceKey.create(Registries.RECIPE, recipe), EmptyRecipe.empty(), null);
    }
}

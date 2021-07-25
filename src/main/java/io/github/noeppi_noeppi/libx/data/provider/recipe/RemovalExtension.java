package io.github.noeppi_noeppi.libx.data.provider.recipe;

import com.google.gson.JsonObject;
import io.github.noeppi_noeppi.libx.impl.recipe.EmptyRecipe;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface RemovalExtension extends RecipeExtension {

    default void remove(ResourceLocation recipe) {
        this.consumer().accept(new FinishedRecipe() {
            
            @Override
            public void serializeRecipeData(@Nonnull JsonObject json) {
                //
            }

            @Nonnull
            @Override
            public ResourceLocation getId() {
                return recipe;
            }

            @Nonnull
            @Override
            public RecipeSerializer<?> getType() {
                return EmptyRecipe.Serializer.INSTANCE;
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId() {
                return null;
            }
        });
    }
}

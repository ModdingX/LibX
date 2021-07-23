package io.github.noeppi_noeppi.libx.data.provider.recipe;

import com.google.gson.JsonObject;
import io.github.noeppi_noeppi.libx.impl.recipe.EmptyRecipe;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface RemovalExtension extends RecipeExtension {

    default void remove(ResourceLocation recipe) {
        this.consumer().accept(new IFinishedRecipe() {
            
            @Override
            public void serialize(@Nonnull JsonObject json) {
                //
            }

            @Nonnull
            @Override
            public ResourceLocation getID() {
                return recipe;
            }

            @Nonnull
            @Override
            public IRecipeSerializer<?> getSerializer() {
                return EmptyRecipe.Serializer.INSTANCE;
            }

            @Nullable
            @Override
            public JsonObject getAdvancementJson() {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementID() {
                return null;
            }
        });
    }
}

package org.moddingx.libx.impl.crafting.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.moddingx.libx.LibX;
import org.moddingx.libx.codec.MoreStreamCodecs;

import javax.annotation.Nonnull;

public class EmptyRecipe implements Recipe<RecipeInput> {
    
    public static final Identifier ID = LibX.getInstance().id("empty");
    public static final RecipeType<EmptyRecipe> TYPE = RecipeType.simple(ID);
    public static final RecipeSerializer<EmptyRecipe> SERIALIZER = new RecipeSerializer<>(
            MapCodec.<EmptyRecipe>unit(EmptyRecipe::new),
            MoreStreamCodecs.<RegistryFriendlyByteBuf, EmptyRecipe>unit(EmptyRecipe::new)
    );

    private EmptyRecipe() {}

    @Override
    public boolean matches(@Nonnull RecipeInput inv, @Nonnull Level level) {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull RecipeInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean showNotification() {
        return true;
    }

    @Nonnull
    @Override
    public String group() {
        return "";
    }

    @Nonnull
    @Override
    public RecipeType<? extends Recipe<RecipeInput>> getType() {
        return TYPE;
    }

    @Nonnull
    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Nonnull
    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Nonnull
    @Override
    public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public boolean isSpecial() {
        return false;
    }

    public static EmptyRecipe empty() {
        return new EmptyRecipe();
    }
}

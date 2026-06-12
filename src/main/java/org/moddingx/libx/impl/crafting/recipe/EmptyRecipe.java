package org.moddingx.libx.impl.crafting.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.moddingx.libx.LibX;
import org.moddingx.libx.codec.MoreStreamCodecs;

import javax.annotation.Nonnull;

public class EmptyRecipe implements Recipe<RecipeInput> {
    
    public static final Identifier ID = LibX.getInstance().resource("empty");
    public static final RecipeType<EmptyRecipe> TYPE = RecipeType.simple(ID);

    private EmptyRecipe() {}

    @Override
    public boolean matches(@Nonnull RecipeInput inv, @Nonnull Level level) {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull RecipeInput input, @Nonnull HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
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
        return Serializer.INSTANCE;
    }

    @Override
    public boolean isSpecial() {
        return false;
    }
    
    public static EmptyRecipe empty() {
        return new EmptyRecipe();
    }
    
    public static class Serializer implements RecipeSerializer<EmptyRecipe> {

        public static final Serializer INSTANCE = new Serializer();
        
        private Serializer() {}

        @Nonnull
        @Override
        public MapCodec<EmptyRecipe> codec() {
            return MapCodec.unit(EmptyRecipe::new);
        }

        @Nonnull
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, EmptyRecipe> streamCodec() {
            return MoreStreamCodecs.unit(EmptyRecipe::new);
        }
    }
}
